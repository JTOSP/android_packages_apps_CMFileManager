/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyanogenmod.explorer.ui.policy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.Spanned;

import com.cyanogenmod.explorer.R;
import com.cyanogenmod.explorer.console.ExecutionException;
import com.cyanogenmod.explorer.console.RelaunchableException;
import com.cyanogenmod.explorer.listeners.OnRequestRefreshListener;
import com.cyanogenmod.explorer.listeners.OnSelectionListener;
import com.cyanogenmod.explorer.model.FileSystemObject;
import com.cyanogenmod.explorer.util.CommandHelper;
import com.cyanogenmod.explorer.util.DialogHelper;
import com.cyanogenmod.explorer.util.ExceptionUtil;
import com.cyanogenmod.explorer.util.ExceptionUtil.OnRelaunchCommandResult;
import com.cyanogenmod.explorer.util.FileHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * A class with the convenience methods for resolve delete related actions
 */
public final class DeleteActionPolicy extends ActionsPolicy {

    /**
     * Method that remove an existing file system object.
     *
     * @param ctx The current context
     * @param fso The file system object to remove
     * @param onSelectionListener The listener for obtain selection information (required)
     * @param onRequestRefreshListener The listener for request a refresh (optional)
     */
    public static void removeFileSystemObject(
            final Context ctx, final FileSystemObject fso,
            final OnSelectionListener onSelectionListener,
            final OnRequestRefreshListener onRequestRefreshListener) {
        // Generate an array and invoke internal method
        List<FileSystemObject> files = new ArrayList<FileSystemObject>(1);
        files.add(fso);
        removeFileSystemObjects(ctx, files, onSelectionListener, onRequestRefreshListener);
    }

    /**
     * Method that remove an existing file system object.
     *
     * @param ctx The current context
     * @param files The list of files to remove
     * @param onSelectionListener The listener for obtain selection information (required)
     * @param onRequestRefreshListener The listener for request a refresh (optional)
     */
    public static void removeFileSystemObjects(
            final Context ctx, final List<FileSystemObject> files,
            final OnSelectionListener onSelectionListener,
            final OnRequestRefreshListener onRequestRefreshListener) {

        // Ask the user before remove
        AlertDialog dialog =DialogHelper.createYesNoDialog(
            ctx, R.string.actions_ask_undone_operation,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface alertDialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        // Remove the items
                        removeFileSystemObjectsInBackground(
                                ctx,
                                files,
                                onSelectionListener,
                                onRequestRefreshListener);
                    }
                }
           });
        dialog.show();
    }

    /**
     * Method that remove an existing file system object in background.
     *
     * @param ctx The current context
     * @param files The list of files to remove
     * @param onSelectionListener The listener for obtain selection information (optional)
     * @param onRequestRefreshListener The listener for request a refresh (optional)
     * @hide
     */
    static void removeFileSystemObjectsInBackground(
            final Context ctx, final List<FileSystemObject> files,
            final OnSelectionListener onSelectionListener,
            final OnRequestRefreshListener onRequestRefreshListener) {

        // Some previous checks prior to execute
        // 1.- Check the operation consistency (only if it is viable)
        if (onSelectionListener != null) {
            final String currentDirectory = onSelectionListener.onRequestCurrentDir();
            if (!checkRemoveConsistency(ctx, files, currentDirectory)) {
                return;
            }
        }
        // 2.- Sort the items by path to avoid delete parents fso prior to child fso
        final List<FileSystemObject> sortedFsos  = new ArrayList<FileSystemObject>(files);
        Collections.sort(sortedFsos, new Comparator<FileSystemObject>() {
            @Override
            public int compare(FileSystemObject lhs, FileSystemObject rhs) {
                return lhs.compareTo(rhs) * -1; //Reverse
            }
        });

        // The callable interface
        final BackgroundCallable callable = new BackgroundCallable() {
            // The current items
            private int mCurrent = 0;
            final Context mCtx = ctx;
            final List<FileSystemObject> mFiles = sortedFsos;
            final OnRequestRefreshListener mOnRequestRefreshListener = onRequestRefreshListener;

            final Object mSync = new Object();
            Throwable mCause;

            @Override
            public int getDialogTitle() {
                return R.string.waiting_dialog_deleting_title;
            }
            @Override
            public int getDialogIcon() {
                return R.drawable.ic_holo_light_operation;
            }
            @Override
            public boolean isDialogCancelable() {
                return false;
            }

            @Override
            public Spanned requestProgress() {
                FileSystemObject fso = this.mFiles.get(this.mCurrent);

                // Return the current operation
                String progress =
                      this.mCtx.getResources().
                          getString(
                              R.string.waiting_dialog_deleting_msg,
                              fso.getFullPath());
                return Html.fromHtml(progress);
            }

            @Override
            public void onSuccess() {
                //Operation complete. Refresh
                if (this.mOnRequestRefreshListener != null) {
                  // The reference is not the same, so refresh the complete navigation view
                  this.mOnRequestRefreshListener.onRequestRefresh(null);
                }
                ActionsPolicy.showOperationSuccessMsg(ctx);
            }

            @Override
            public void doInBackground(Object... params) throws Throwable {
                this.mCause = null;

                // This method expect to receive
                // 1.- BackgroundAsyncTask
                BackgroundAsyncTask task = (BackgroundAsyncTask)params[0];

                int cc = this.mFiles.size();
                for (int i = 0; i < cc; i++) {
                    FileSystemObject fso = this.mFiles.get(i);

                    doOperation(this.mCtx, fso);

                    // Next file
                    this.mCurrent++;
                    if (this.mCurrent < this.mFiles.size()) {
                        task.onRequestProgress();
                    }
                }
            }

            /**
             * Method that deletes the file or directory
             *
             * @param ctx The current context
             * @param fso The file or folder to be deleted
             */
            @SuppressWarnings("hiding")
            private void doOperation(
                    final Context ctx, final FileSystemObject fso) throws Throwable {
                try {
                    // Remove the item
                    if (FileHelper.isDirectory(fso)) {
                        CommandHelper.deleteDirectory(ctx, fso.getFullPath(), null);
                    } else {
                        CommandHelper.deleteFile(ctx, fso.getFullPath(), null);
                    }
                } catch (Exception e) {
                    // Need to be relaunched?
                    if (e instanceof RelaunchableException) {
                        OnRelaunchCommandResult rl = new OnRelaunchCommandResult() {
                            @Override
                            @SuppressWarnings("unqualified-field-access")
                            public void onSuccess() {
                                synchronized (mSync) {
                                    mSync.notify();
                                }
                            }

                            @Override
                            @SuppressWarnings("unqualified-field-access")
                            public void onFailed(Throwable cause) {
                                mCause = cause;
                                synchronized (mSync) {
                                    mSync.notify();
                                }
                            }
                            @Override
                            @SuppressWarnings("unqualified-field-access")
                            public void onCanceled() {
                                synchronized (mSync) {
                                    mSync.notify();
                                }
                            }
                        };

                        // Translate the exception (and wait for the result)
                        ExceptionUtil.translateException(ctx, e, false, true, rl);
                        synchronized (this.mSync) {
                            this.mSync.wait();
                        }

                        // Persist the exception?
                        if (this.mCause != null) {
                            // The exception must be elevated
                            throw this.mCause;
                        }

                    } else {
                        // The exception must be elevated
                        throw e;
                    }
                }

                // Check that the operation was completed retrieving the deleted fso
                boolean failed = false;
                try {
                    CommandHelper.getFileInfo(ctx, fso.getFullPath(), false, null);

                    // Failed. The file still exists
                    failed = true;

                } catch (Throwable e) {
                    // Operation complete successfully
                }
                if (failed) {
                    throw new ExecutionException(
                            String.format(
                                    "Failed to delete file: %s", fso.getFullPath())); //$NON-NLS-1$
                }
            }
        };
        final BackgroundAsyncTask task = new BackgroundAsyncTask(ctx, callable);

        // Execute background task
        task.execute(task);
    }

    /**
     * Method that check the consistency of delete operations.<br/>
     * <br/>
     * The method checks the following rules:<br/>
     * <ul>
     * <li>Any of the files of the move or delete operation can not include the
     * current directory.</li>
     * </ul>
     *
     * @param ctx The current context
     * @param files The list of source/destination files
     * @param currentDirectory The current directory
     * @return boolean If the consistency is validate successfully
     */
    private static boolean checkRemoveConsistency(
            Context ctx, List<FileSystemObject> files, String currentDirectory) {
        int cc = files.size();
        for (int i = 0; i < cc; i++) {
            FileSystemObject fso = files.get(i);

            // 1.- Current directory can't be deleted
            if (currentDirectory.startsWith(fso.getFullPath())) {
                // Operation not allowed
                AlertDialog dialog =
                        DialogHelper.createErrorDialog(
                                ctx, R.string.msgs_unresolved_inconsistencies);
                dialog.show();
                return false;
            }
        }
        return true;
    }
}