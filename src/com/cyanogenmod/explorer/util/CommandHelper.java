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

package com.cyanogenmod.explorer.util;

import android.content.Context;

import com.cyanogenmod.explorer.commands.AsyncResultListener;
import com.cyanogenmod.explorer.commands.ChangeCurrentDirExecutable;
import com.cyanogenmod.explorer.commands.ChangeOwnerExecutable;
import com.cyanogenmod.explorer.commands.ChangePermissionsExecutable;
import com.cyanogenmod.explorer.commands.CompressExecutable;
import com.cyanogenmod.explorer.commands.CopyExecutable;
import com.cyanogenmod.explorer.commands.CreateDirExecutable;
import com.cyanogenmod.explorer.commands.CreateFileExecutable;
import com.cyanogenmod.explorer.commands.CurrentDirExecutable;
import com.cyanogenmod.explorer.commands.DeleteDirExecutable;
import com.cyanogenmod.explorer.commands.DeleteFileExecutable;
import com.cyanogenmod.explorer.commands.DiskUsageExecutable;
import com.cyanogenmod.explorer.commands.EchoExecutable;
import com.cyanogenmod.explorer.commands.ExecExecutable;
import com.cyanogenmod.explorer.commands.Executable;
import com.cyanogenmod.explorer.commands.FindExecutable;
import com.cyanogenmod.explorer.commands.FolderUsageExecutable;
import com.cyanogenmod.explorer.commands.GroupsExecutable;
import com.cyanogenmod.explorer.commands.IdentityExecutable;
import com.cyanogenmod.explorer.commands.LinkExecutable;
import com.cyanogenmod.explorer.commands.ListExecutable;
import com.cyanogenmod.explorer.commands.MountExecutable;
import com.cyanogenmod.explorer.commands.MountPointInfoExecutable;
import com.cyanogenmod.explorer.commands.MoveExecutable;
import com.cyanogenmod.explorer.commands.ParentDirExecutable;
import com.cyanogenmod.explorer.commands.ProcessIdExecutable;
import com.cyanogenmod.explorer.commands.QuickFolderSearchExecutable;
import com.cyanogenmod.explorer.commands.ReadExecutable;
import com.cyanogenmod.explorer.commands.ResolveLinkExecutable;
import com.cyanogenmod.explorer.commands.SIGNAL;
import com.cyanogenmod.explorer.commands.SendSignalExecutable;
import com.cyanogenmod.explorer.commands.SyncResultExecutable;
import com.cyanogenmod.explorer.commands.UncompressExecutable;
import com.cyanogenmod.explorer.commands.WritableExecutable;
import com.cyanogenmod.explorer.commands.WriteExecutable;
import com.cyanogenmod.explorer.commands.shell.InvalidCommandDefinitionException;
import com.cyanogenmod.explorer.console.CommandNotFoundException;
import com.cyanogenmod.explorer.console.Console;
import com.cyanogenmod.explorer.console.ConsoleAllocException;
import com.cyanogenmod.explorer.console.ConsoleBuilder;
import com.cyanogenmod.explorer.console.ExecutionException;
import com.cyanogenmod.explorer.console.InsufficientPermissionsException;
import com.cyanogenmod.explorer.console.NoSuchFileOrDirectory;
import com.cyanogenmod.explorer.console.OperationTimeoutException;
import com.cyanogenmod.explorer.console.ReadOnlyFilesystemException;
import com.cyanogenmod.explorer.model.DiskUsage;
import com.cyanogenmod.explorer.model.FileSystemObject;
import com.cyanogenmod.explorer.model.FolderUsage;
import com.cyanogenmod.explorer.model.Group;
import com.cyanogenmod.explorer.model.Identity;
import com.cyanogenmod.explorer.model.MountPoint;
import com.cyanogenmod.explorer.model.Permissions;
import com.cyanogenmod.explorer.model.Query;
import com.cyanogenmod.explorer.model.SearchResult;
import com.cyanogenmod.explorer.model.User;
import com.cyanogenmod.explorer.preferences.CompressionMode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;


/**
 * A helper class with useful methods for deal with commands.
 */
public final class CommandHelper {

    /**
     * Constructor of <code>CommandHelper</code>.
     */
    private CommandHelper() {
        super();
    }

    /**
     * Method that changes the current directory of the shell.
     *
     * @param context The current context (needed if console == null)
     * @param dst The new directory
     * @return boolean The operation result
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see ChangeCurrentDirExecutable
     */
    public static boolean changeCurrentDir(Context context, String dst, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        ChangeCurrentDirExecutable executable =
                c.getExecutableFactory().newCreator().createChangeCurrentDirExecutable(dst);
        execute(context, executable, c);
        return executable.getResult().booleanValue();
    }

    /**
     * Method that changes the owner of a file system object.
     *
     * @param context The current context (needed if console == null)
     * @param src The file system object to change its permissions
     * @param user The new user owner of the file system object
     * @param group The new group owner of the file system object
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return boolean The operation result
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @throws ReadOnlyFilesystemException If the operation writes in a read-only filesystem
     * @see ChangeOwnerExecutable
     */
    public static boolean changeOwner(
            Context context, String src, User user, Group group, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException, ReadOnlyFilesystemException {
        Console c = ensureConsole(context, console);
        ChangeOwnerExecutable executable =
                c.getExecutableFactory().
                    newCreator().createChangeOwnerExecutable(src, user, group);
        writableExecute(context, executable, c);
        return executable.getResult().booleanValue();
    }

    /**
     * Method that changes the permissions of a file system object.
     *
     * @param context The current context (needed if console == null)
     * @param src The file system object to change its permissions
     * @param permissions The new permissions of the file system object
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return boolean The operation result
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @throws ReadOnlyFilesystemException If the operation writes in a read-only filesystem
     * @see ChangePermissionsExecutable
     */
    public static boolean changePermissions(
            Context context, String src, Permissions permissions, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException, ReadOnlyFilesystemException {
        Console c = ensureConsole(context, console);
        ChangePermissionsExecutable executable =
                c.getExecutableFactory().newCreator().
                    createChangePermissionsExecutable(src, permissions);
        writableExecute(context, executable, c);
        return executable.getResult().booleanValue();
    }

    /**
     * Method that creates a directory.
     *
     * @param context The current context (needed if console == null)
     * @param directory The directory to create
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return boolean The operation result
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @throws ReadOnlyFilesystemException If the operation writes in a read-only filesystem
     * @see CreateDirExecutable
     */
    public static boolean createDirectory(Context context, String directory, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException, ReadOnlyFilesystemException {
        Console c = ensureConsole(context, console);
        CreateDirExecutable executable =
                c.getExecutableFactory().newCreator().createCreateDirectoryExecutable(directory);
        writableExecute(context, executable, c);
        return executable.getResult().booleanValue();
    }

    /**
     * Method that creates a file.
     *
     * @param context The current context (needed if console == null)
     * @param file The file to create
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return boolean The operation result
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @throws ReadOnlyFilesystemException If the operation writes in a read-only filesystem
     * @see CreateFileExecutable
     */
    public static boolean createFile(Context context, String file, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException, ReadOnlyFilesystemException {
        Console c = ensureConsole(context, console);
        CreateFileExecutable executable =
                c.getExecutableFactory().newCreator().createCreateFileExecutable(file);
        writableExecute(context, executable, c);
        return executable.getResult().booleanValue();
    }

    /**
     * Method that deletes a directory.
     *
     * @param context The current context (needed if console == null)
     * @param directory The directory to delete
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return boolean The operation result
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @throws ReadOnlyFilesystemException If the operation writes in a read-only filesystem
     * @see DeleteDirExecutable
     */
    public static boolean deleteDirectory(Context context, String directory, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException, ReadOnlyFilesystemException {
        Console c = ensureConsole(context, console);
        DeleteDirExecutable executable =
                c.getExecutableFactory().newCreator().createDeleteDirExecutable(directory);
        writableExecute(context, executable, c);
        return executable.getResult().booleanValue();
    }

    /**
     * Method that deletes a file.
     *
     * @param context The current context (needed if console == null)
     * @param file The file to delete
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return boolean The operation result
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @throws ReadOnlyFilesystemException If the operation writes in a read-only filesystem
     * @see DeleteFileExecutable
     */
    public static boolean deleteFile(Context context, String file, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException, ReadOnlyFilesystemException {
        Console c = ensureConsole(context, console);
        DeleteFileExecutable executable =
                c.getExecutableFactory().newCreator().createDeleteFileExecutable(file);
        writableExecute(context, executable, c);
        return executable.getResult().booleanValue();
    }

    /**
     * Method that retrieves the absolute path of a file or directory.
     *
     * @param context The current context (needed if console == null)
     * @param path The short path
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return String The absolute path of the directory
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see ResolveLinkExecutable
     */
    public static String getAbsolutePath(Context context, String path, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        ResolveLinkExecutable executable =
                c.getExecutableFactory().newCreator().createResolveLinkExecutable(path);
        execute(context, executable, c);
        FileSystemObject fso = executable.getResult();
        if (fso == null) {
            return null;
        }
        return fso.getFullPath();
    }

    /**
     * Method that resolves a symlink to its real file system object.
     *
     * @param context The current context (needed if console == null)
     * @param symlink The link to be resolved
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return String The resolved link
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see ResolveLinkExecutable
     */
    public static FileSystemObject resolveSymlink(Context context, String symlink, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        ResolveLinkExecutable executable =
                c.getExecutableFactory().newCreator().createResolveLinkExecutable(symlink);
        execute(context, executable, c);
        return executable.getResult();
    }

    /**
     * Method that retrieves the current directory of the shell.
     *
     * @param context The current context (needed if console == null)
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return String The current directory
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see CurrentDirExecutable
     */
    public static String getCurrentDir(Context context, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        CurrentDirExecutable executable =
                c.getExecutableFactory().newCreator().createCurrentDirExecutable();
        execute(context, executable, c);
        return executable.getResult();
    }

    /**
     * Method that retrieves the information of a file system object.
     *
     * @param context The current context (needed if console == null)
     * @param src The file system object
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return FileSystemObject The file system object reference
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see ListExecutable
     */
    public static FileSystemObject getFileInfo(Context context, String src, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        return getFileInfo(context, src, true, console);
    }

    /**
     * Method that retrieves the information of a file system object.
     *
     * @param context The current context (needed if console == null)
     * @param src The file system object
     * @param followSymlinks It should be follow the symlinks
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return FileSystemObject The file system object reference
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see ListExecutable
     */
    public static FileSystemObject getFileInfo(
            Context context, String src, boolean followSymlinks, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        ListExecutable executable =
                c.getExecutableFactory().
                    newCreator().createFileInfoExecutable(src, followSymlinks);
        execute(context, executable, c);
        List<FileSystemObject> files = executable.getResult();
        if (files != null && files.size() > 0) {
            // Resolve symlinks prior to return the object
            FileHelper.resolveSymlinks(context, files);
            return files.get(0);
        }
        return null;
    }

    /**
     * Method that retrieves the information of the groups of the current user.
     *
     * @param context The current context (needed if console == null)
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return List<Group> The groups of the current user
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see GroupsExecutable
     */
    public static List<Group> getGroups(Context context, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        GroupsExecutable executable =
                c.getExecutableFactory().newCreator().createGroupsExecutable();
        execute(context, executable, c);
        return executable.getResult();
    }

    /**
    * Method that retrieves the identity of the current user.
    *
    * @param context The current context (needed if console == null)
    * @param console The console in which execute the program. <code>null</code>
    * to attach to the default console
    * @return Identity The identity of the current user
    * @throws FileNotFoundException If the initial directory not exists
    * @throws IOException If initial directory can't not be checked
    * @throws InvalidCommandDefinitionException If the command has an invalid definition
    * @throws NoSuchFileOrDirectory If the file or directory was not found
    * @throws ConsoleAllocException If the console can't be allocated
    * @throws InsufficientPermissionsException If an operation requires elevated permissions
    * @throws CommandNotFoundException If the command was not found
    * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
    * @throws ExecutionException If the operation returns a invalid exit code
    * @see IdentityExecutable
    */
   public static Identity getIdentity(Context context, Console console)
           throws FileNotFoundException, IOException, ConsoleAllocException,
           NoSuchFileOrDirectory, InsufficientPermissionsException,
           CommandNotFoundException, OperationTimeoutException,
           ExecutionException, InvalidCommandDefinitionException {
       Console c = ensureConsole(context, console);
       IdentityExecutable executable =
               c.getExecutableFactory().newCreator().createIdentityExecutable();
       execute(context, executable, c);
       return executable.getResult();
   }

   /**
    * Method that creates a symlink of an other file system object.
    *
    * @param context The current context (needed if console == null)
    * @param src The absolute path to the source fso
     * @param link The absolute path to the link fso
    * @param console The console in which execute the program. <code>null</code>
    * to attach to the default console
    * @return boolean The operation result
    * @throws FileNotFoundException If the initial directory not exists
    * @throws IOException If initial directory can't not be checked
    * @throws InvalidCommandDefinitionException If the command has an invalid definition
    * @throws NoSuchFileOrDirectory If the file or directory was not found
    * @throws ConsoleAllocException If the console can't be allocated
    * @throws InsufficientPermissionsException If an operation requires elevated permissions
    * @throws CommandNotFoundException If the command was not found
    * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
    * @throws ExecutionException If the operation returns a invalid exit code
    * @throws ReadOnlyFilesystemException If the operation writes in a read-only filesystem
    * @see LinkExecutable
    */
   public static boolean createLink(Context context, String src, String link, Console console)
           throws FileNotFoundException, IOException, ConsoleAllocException,
           NoSuchFileOrDirectory, InsufficientPermissionsException,
           CommandNotFoundException, OperationTimeoutException,
           ExecutionException, InvalidCommandDefinitionException, ReadOnlyFilesystemException {
       Console c = ensureConsole(context, console);
       LinkExecutable executable =
               c.getExecutableFactory().newCreator().createLinkExecutable(src, link);
       writableExecute(context, executable, c);
       return executable.getResult().booleanValue();
   }

    /**
     * Method that retrieves the parent directory of a file system object.
     *
     * @param context The current context (needed if console == null)
     * @param src The file system object
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return String The current directory
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see ParentDirExecutable
     */
    public static String getParentDir(Context context, String src, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        ParentDirExecutable executable =
                c.getExecutableFactory().newCreator().createParentDirExecutable(src);
        execute(context, executable, c);
        return executable.getResult();
    }

    /**
     * Method that retrieves the value of a variable.
     *
     * @param context The current context (needed if console == null)
     * @param msg The message to echo. This message can have one or multiple variables
     * and text. xe: "This is $VAR_1 the value of $VAR2" or simple "$PATH"
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return String The value  of the variable
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see EchoExecutable
     */
    public static String getVariable(Context context, String msg, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        EchoExecutable executable =
                c.getExecutableFactory().newCreator().createEchoExecutable(msg);
        execute(context, executable, c);
        return executable.getResult();
    }

    /**
     * Method that lists a directory.
     *
     * @param context The current context (needed if console == null)
     * @param directory The path of the directory to list
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return List<FileSystemObject> The list of files of the directory
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see ListExecutable
     */
    public static List<FileSystemObject> listFiles(
            Context context, String directory, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        ListExecutable executable =
                c.getExecutableFactory().newCreator().
                    createListExecutable(directory);
        execute(context, executable, c);
        List<FileSystemObject> result = executable.getResult();
        FileHelper.resolveSymlinks(context, result);
        return result;
    }

    /**
     * Method that moves a file system object.
     *
     * @param context The current context (needed if console == null)
     * @param src The file system object to move
     * @param dst The destination file system object
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return boolean The operation result
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @throws ReadOnlyFilesystemException If the operation writes in a read-only filesystem
     * @see MoveExecutable
     */
    public static boolean move(Context context, String src, String dst, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException, ReadOnlyFilesystemException {
        Console c = ensureConsole(context, console);
        MoveExecutable executable =
                c.getExecutableFactory().newCreator().createMoveExecutable(src, dst);
        writableExecute(context, executable, c);
        return executable.getResult().booleanValue();
    }

    /**
     * Method that copies a file system object.
     *
     * @param context The current context (needed if console == null)
     * @param src The file system object to copy
     * @param dst The destination file system object
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return boolean The operation result
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @throws ReadOnlyFilesystemException If the operation writes in a read-only filesystem
     * @see CopyExecutable
     */
    public static boolean copy(Context context, String src, String dst, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException, ReadOnlyFilesystemException {
        Console c = ensureConsole(context, console);
        CopyExecutable executable =
                c.getExecutableFactory().newCreator().createCopyExecutable(src, dst);
        writableExecute(context, executable, c);
        return executable.getResult().booleanValue();
    }

    /**
     * Method that executes a command.
     *
     * @param context The current context (needed if console == null)
     * @param cmd The command to execute
     * @param asyncResultListener The partial result listener
     * @param console The console in which execute the program.
     * <code>null</code> to attach to the default console
     * @return AsyncResultProgram The command executed in background
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see ExecExecutable
     */
    public static ExecExecutable exec(
            Context context, String cmd, AsyncResultListener asyncResultListener, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        ExecExecutable executable =
                c.getExecutableFactory().newCreator().
                    createExecExecutable(cmd, asyncResultListener);
        execute(context, executable, c);
        return executable;
    }

    /**
     * Method that does a search in a directory tree seeking for some terms.
     *
     * @param context The current context (needed if console == null)
     * @param directory The "absolute" directory where start the search
     * @param search The terms to be searched
     * @param asyncResultListener The partial result listener
     * @param console The console in which execute the program.
     * <code>null</code> to attach to the default console
     * @return AsyncResultProgram The command executed in background
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see SearchResult
     * @see FindExecutable
     */
    public static FindExecutable findFiles(
            Context context, String directory, Query search,
            AsyncResultListener asyncResultListener, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        FindExecutable executable =
                c.getExecutableFactory().newCreator().
                    createFindExecutable(directory, search, asyncResultListener);
        execute(context, executable, c);
        return executable;
    }

    /**
     * Method that compute the disk usage of a folder.
     *
     * @param context The current context (needed if console == null)
     * @param directory The "absolute" directory where start the search
     * @param asyncResultListener The partial result listener
     * @param console The console in which execute the program.
     * <code>null</code> to attach to the default console
     * @return AsyncResultProgram The command executed in background
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see FolderUsage
     * @see FolderUsageExecutable
     */
    public static FolderUsageExecutable getFolderUsage(
            Context context, String directory,
            AsyncResultListener asyncResultListener, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        FolderUsageExecutable executable =
                c.getExecutableFactory().newCreator().
                    createFolderUsageExecutable(directory, asyncResultListener);
        execute(context, executable, c);
        return executable;
    }

    /**
     * Method that retrieves the disk usage of all the mount points.
     *
     * @param context The current context (needed if console == null)
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return List<DiskUsage> The disk usage of all the mount points
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see DiskUsageExecutable
     */
    public static List<DiskUsage> getDiskUsage(Context context, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        DiskUsageExecutable executable =
                c.getExecutableFactory().newCreator().createDiskUsageExecutable();
        execute(context, executable, c);
        return executable.getResult();
    }

    /**
     * Method that retrieves the disk usage of all mount points.
     *
     * @param context The current context (needed if console == null)
     * @param dir The directory of which obtain its disk usage
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return DiskUsage The disk usage information
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see DiskUsageExecutable
     */
    public static DiskUsage getDiskUsage(Context context, String dir, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        DiskUsageExecutable executable =
                c.getExecutableFactory().newCreator().createDiskUsageExecutable(dir);
        execute(context, executable, c);
        List<DiskUsage> du = executable.getResult();
        if (du != null && du.size() > 0) {
            return du.get(0);
        }
        return null;
    }

    /**
     * Method that retrieves the information about all mount points.
     *
     * @param context The current context (needed if console == null)
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return List<MountPoint> The filesystem mount points
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see MountPointInfoExecutable
     */
    public static List<MountPoint> getMountPoints(Context context, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        MountPointInfoExecutable executable =
                c.getExecutableFactory().newCreator().createMountPointInfoExecutable();
        execute(context, executable, c);
        return executable.getResult();
    }

    /**
     * Method that re-mounts a filesystem from his mount point info.
     *
     * @param context The current context (needed if console == null)
     * @param mp The mount point to re-mount
     * @param rw Indicates if the operation re-mounted the device as read-write
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return boolean The operation result
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see MountExecutable
     */
    public static boolean remount(Context context, MountPoint mp, boolean rw, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        MountExecutable executable =
                c.getExecutableFactory().newCreator().createMountExecutable(mp, rw);
        execute(context, executable, c);
        return executable.getResult().booleanValue();
    }

    /**
     * Method that makes a quick folder search for the passed expression.
     *
     * @param context The current context (needed if console == null)
     * @param regexp The expression to search
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return List<String> The list of directories found
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see QuickFolderSearchExecutable
     */
    public static List<String> quickFolderSearch(Context context, String regexp, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        QuickFolderSearchExecutable executable =
                c.getExecutableFactory().newCreator().createQuickFolderSearchExecutable(regexp);
        execute(context, executable, c);
        return executable.getResult();
    }

    /**
     * Method that retrieves the process identifier of a process (a program
     * owned by the main process of this application).
     *
     * @param context The current context (needed if console == null)
     * @param pid The process id of the shell where the command is running
     * @param processName The process name
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return Integer The process identifier of the program or <code>null</code> if not exists
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see ProcessIdExecutable
     */
    public static Integer getProcessId(
            Context context, int pid, String processName, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        ProcessIdExecutable executable =
                c.getExecutableFactory().newCreator().createProcessIdExecutable(pid, processName);
        execute(context, executable, c);
        return executable.getResult();
    }

    /**
     * Method that send a signal to a process.
     *
     * @param context The current context (needed if console == null)
     * @param process The process which to send the signal
     * @param signal The signal to send
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see ProcessIdExecutable
     */
    public static void sendSignal(
            Context context, int process, SIGNAL signal, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        SendSignalExecutable executable =
                c.getExecutableFactory().newCreator().createSendSignalExecutable(process, signal);
        execute(context, executable, c);
    }

    /**
     * Method that send a kill signal to a process.
     *
     * @param context The current context (needed if console == null)
     * @param process The process which to send the signal
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see ProcessIdExecutable
     */
    public static void sendSignal(
            Context context, int process, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        SendSignalExecutable executable =
                c.getExecutableFactory().newCreator().createKillExecutable(process);
        execute(context, executable, c);
    }

    /**
     * Method that read data from disk.
     *
     * @param context The current context (needed if console == null)
     * @param file The file where to read the data
     * @param asyncResultListener The partial result listener
     * @param console The console in which execute the program.
     * <code>null</code> to attach to the default console
     * @return AsyncResultProgram The command executed in background
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @see "byte[]"
     * @see ReadExecutable
     */
    public static ReadExecutable read(
            Context context, String file,
            AsyncResultListener asyncResultListener, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        ReadExecutable executable =
                c.getExecutableFactory().newCreator().
                    createReadExecutable(file, asyncResultListener);
        execute(context, executable, c);
        return executable;
    }

    /**
     * Method that writes data to disk.
     *
     * @param context The current context (needed if console == null)
     * @param file The file where to write the data
     * @param asyncResultListener The partial result listener
     * @param console The console in which execute the program.
     * <code>null</code> to attach to the default console
     * @return AsyncResultProgram The command executed in background
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @throws ReadOnlyFilesystemException If the operation writes in a read-only filesystem
     * @see WriteExecutable
     */
    public static WriteExecutable write(
            Context context, String file,
            AsyncResultListener asyncResultListener, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException, ReadOnlyFilesystemException {
        Console c = ensureConsole(context, console);
        // Prior to write to disk the data, ensure that can write to the disk using
        // createFile method
        //- Create
        CreateFileExecutable executable1 =
                c.getExecutableFactory().newCreator().createCreateFileExecutable(file);
        writableExecute(context, executable1, c);
        if (executable1.getResult().booleanValue()) {
            //- Write
            WriteExecutable executable2 =
                    c.getExecutableFactory().newCreator().
                        createWriteExecutable(file, asyncResultListener);
            execute(context, executable2, c);
            return executable2;
        }
        throw new ExecutionException(String.format("Fail to create file %s", file)); //$NON-NLS-1$
    }

    /**
     * Method that archive-compress file system objects.
     *
     * @param context The current context (needed if console == null)
     * @param mode The compression mode
     * @param dst The destination compressed file
     * @param src The array of source files to compress
     * @param asyncResultListener The partial result listener
     * @param console The console in which execute the program.
     * <code>null</code> to attach to the default console
     * @return AsyncResultProgram The command executed in background
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @throws ReadOnlyFilesystemException If the operation writes in a read-only filesystem
     * @see CompressExecutable
     */
    public static CompressExecutable compress(
            Context context, CompressionMode mode, String dst, String[] src,
            AsyncResultListener asyncResultListener, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException, ReadOnlyFilesystemException {
        Console c = ensureConsole(context, console);

        CompressExecutable executable1 =
                c.getExecutableFactory().newCreator().
                    createCompressExecutable(mode, dst, src, asyncResultListener);

        // Prior to write to disk the data, ensure that can write to the disk using
        // createFile method
        //- Create
        String compressOutFile = executable1.getOutCompressedFile();
        CreateFileExecutable executable2 =
                c.getExecutableFactory().
                    newCreator().
                        createCreateFileExecutable(compressOutFile);
        writableExecute(context, executable2, c);
        if (executable2.getResult().booleanValue()) {
            //- Compress
            execute(context, executable1, c);
            return executable1;
        }
        throw new ExecutionException(
                String.format("Fail to create file %s", compressOutFile)); //$NON-NLS-1$
    }

    /**
     * Method that compress a file system object.
     *
     * @param context The current context (needed if console == null)
     * @param mode The compression mode
     * @param src The file to compress
     * @param asyncResultListener The partial result listener
     * @param console The console in which execute the program.
     * <code>null</code> to attach to the default console
     * @return AsyncResultProgram The command executed in background
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @throws ReadOnlyFilesystemException If the operation writes in a read-only filesystem
     * @see CompressExecutable
     */
    public static CompressExecutable compress(
            Context context, CompressionMode mode, String src,
            AsyncResultListener asyncResultListener, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException, ReadOnlyFilesystemException {
        Console c = ensureConsole(context, console);

        CompressExecutable executable1 =
                c.getExecutableFactory().newCreator().
                    createCompressExecutable(mode, src, asyncResultListener);

        // Prior to write to disk the data, ensure that can write to the disk using
        // createFile method
        //- Create
        String compressOutFile = executable1.getOutCompressedFile();
        CreateFileExecutable executable2 =
                c.getExecutableFactory().
                    newCreator().
                        createCreateFileExecutable(compressOutFile);
        writableExecute(context, executable2, c);
        if (executable2.getResult().booleanValue()) {
            //- Compress
            execute(context, executable1, c);
            return executable1;
        }
        throw new ExecutionException(
                String.format("Fail to compress to file %s", compressOutFile)); //$NON-NLS-1$
    }

    /**
     * Method that uncompress file system objects.
     *
     * @param context The current context (needed if console == null)
     * @param src The file to compress
     * @param dst The destination file of folder (if null this method resolve with the best
     * fit based on the src)
     * @param asyncResultListener The partial result listener
     * @param console The console in which execute the program.
     * <code>null</code> to attach to the default console
     * @return AsyncResultProgram The command executed in background
     * @throws FileNotFoundException If the initial directory not exists
     * @throws IOException If initial directory can't not be checked
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @throws ReadOnlyFilesystemException If the operation writes in a read-only filesystem
     * @see CompressExecutable
     */
    public static UncompressExecutable uncompress(
            Context context, String src, String dst,
            AsyncResultListener asyncResultListener, Console console)
            throws FileNotFoundException, IOException, ConsoleAllocException,
            NoSuchFileOrDirectory, InsufficientPermissionsException,
            CommandNotFoundException, OperationTimeoutException,
            ExecutionException, InvalidCommandDefinitionException, ReadOnlyFilesystemException {
        Console c = ensureConsole(context, console);

        UncompressExecutable executable1 =
                c.getExecutableFactory().newCreator().
                    createUncompressExecutable(src, dst, asyncResultListener);

        // Prior to write to disk the data, ensure that can write to the disk using
        // createFile or createFolder method

        String compressOutFile = executable1.getOutUncompressedFile();
        WritableExecutable executable2 = null;
        if (executable1.IsArchive()) {
            //- Create Folder
            executable2 =
                    c.getExecutableFactory().
                        newCreator().
                            createCreateDirectoryExecutable(compressOutFile);
        } else {
            //- Create File
            executable2 =
                    c.getExecutableFactory().
                        newCreator().
                            createCreateFileExecutable(compressOutFile);
        }
        writableExecute(context, executable2, c);
        if (((Boolean)executable2.getResult()).booleanValue()) {
            //- Compress
            execute(context, executable1, c);
            return executable1;
        }
        throw new ExecutionException(
                String.format("Fail to uncompress to %s", compressOutFile)); //$NON-NLS-1$
    }

    /**
     * Method that re-execute the command.
     *
     * @param context The current context (needed if console == null)
     * @param executable The executable program to execute
     * @param console The console in which execute the program. <code>null</code>
     * to attach to the default console
     * @return Object The result of the re-execution
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @throws ReadOnlyFilesystemException If the operation writes in a read-only filesystem
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws IOException If initial directory can't not be checked
     * @throws FileNotFoundException If the initial directory not exists
     */
    public static Object reexecute(
            Context context , SyncResultExecutable executable, Console console)
            throws ConsoleAllocException, InsufficientPermissionsException, NoSuchFileOrDirectory,
            OperationTimeoutException, ExecutionException,
            CommandNotFoundException, ReadOnlyFilesystemException,
            FileNotFoundException, IOException, InvalidCommandDefinitionException {
        Console c = ensureConsole(context, console);
        c.execute(executable);
        return executable.getResult();
    }


    /**
     * Method that execute a program.
     *
     * @param context The current context (needed if console == null)
     * @param executable The executable program to execute
     * @param console The console in which execute the program. <code>null</code> to attach
     * to the default console
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     */
    private static void execute(Context context, Executable executable, Console console)
            throws ConsoleAllocException, InsufficientPermissionsException, NoSuchFileOrDirectory,
            OperationTimeoutException, ExecutionException,
            CommandNotFoundException {
        try {
            console.execute(executable);
        } catch (ReadOnlyFilesystemException rofEx) {
            // ReadOnlyFilesystemException don't have sense if command is not writable
            // WritableExecutable must be used with "writableExecute" method
            throw new ExecutionException(rofEx.getMessage(), rofEx);
        }
    }

    /**
     * Method that execute a program that requires write permissions over the filesystem. This
     * method ensure mount/unmount the filesystem before/after executing the operation.
     *
     * @param context The current context (needed if console == null)
     * @param executable The writable executable program to execute
     * @param console The console in which execute the program. <code>null</code> to attach
     * to the default console
     * @throws NoSuchFileOrDirectory If the file or directory was not found
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws CommandNotFoundException If the command was not found
     * @throws OperationTimeoutException If the operation exceeded the maximum time of wait
     * @throws ExecutionException If the operation returns a invalid exit code
     * @throws ReadOnlyFilesystemException If the operation writes in a read-only filesystem
     */
    private static void writableExecute(
            Context context, WritableExecutable executable, Console console)
            throws ConsoleAllocException, InsufficientPermissionsException, NoSuchFileOrDirectory,
            OperationTimeoutException, ExecutionException,
            CommandNotFoundException, ReadOnlyFilesystemException {

        //Retrieve the mount point information to check if a remount operation is required
        boolean needMount = false;
        MountPoint mp = executable.getWritableMountPoint();
        if (mp != null) {
            if (MountPointHelper.isMountAllowed(mp)) {
                if (!MountPointHelper.isReadWrite(mp)) {
                    needMount = true;
                } else {
                    //Mount point is already read-write
                }
            } else {
                //For security or physical reasons the mount point can't be
                //mounted as read-write. Execute the command
                //and notify to the user
            }
        } else {
            //Don't have information about the mount point. Execute the command
            //and notify to the user
        }

        //Create the mount/unmount executables
        MountExecutable mountExecutable = null;
        MountExecutable unmountExecutable = null;
        if (needMount) {
            mountExecutable =
                    console.getExecutableFactory().newCreator().
                        createMountExecutable(mp, true);
            unmountExecutable =
                    console.getExecutableFactory().newCreator().
                        createMountExecutable(mp, false);
        }


        //Execute the commands
        boolean mountExecuted = false;
        try {
            if (needMount) {
                //Execute the mount command
                console.execute(mountExecutable);
                mountExecuted = true;
            }

            //Execute the command
            console.execute(executable);

        } catch (InsufficientPermissionsException ipEx) {
            //Configure the commands to execute
            if (needMount && !mountExecuted) {
                //The failed operation was the mount rw operation
                //This operations is already in the exception in the fifo queue
                ipEx.addExecutable(executable);
            }
            if (needMount) {
                //A mount operation was executed or will be executed
                ipEx.addExecutable(unmountExecutable);
            }

            //Rethrow the exception
            throw ipEx;

        } finally {
            //If previously was a mount successful execution, then execute
            //and unmount operation
            if (mountExecuted) {
                //Execute the unmount command
                console.execute(unmountExecutable);
            }
        }


    }

    /**
     * Method that ensure the console retrieve the default console if a console
     * is not passed.
     *
     * @param context The current context (needed if console == null)
     * @param console The console passed
     * @return Console The console passed if not is null. Otherwise, the default console
     * @throws InsufficientPermissionsException If an operation requires elevated permissions
     * @throws ConsoleAllocException If the console can't be allocated
     * @throws InvalidCommandDefinitionException If the command has an invalid definition
     * @throws IOException If initial directory can't not be checked
     * @throws FileNotFoundException If the initial directory not exists
     */
    private static Console ensureConsole(Context context, Console console)
            throws FileNotFoundException, IOException, InvalidCommandDefinitionException,
            ConsoleAllocException, InsufficientPermissionsException {
        Console c = console;
        if (c == null) {
            c = ConsoleBuilder.getConsole(context);
        }
        return c;
    }

}
