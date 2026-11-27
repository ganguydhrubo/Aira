# Using phone folders granted through the system

On the "Phone folders" page in Settings, choose "Choose a read-only folder" or "Choose a read-write folder", then grant access with the Android system picker. Both kinds of access let Aira find files in that folder and its subfolders, and import ordinary files such as PDFs, images and spreadsheets into the current workspace. For example, after granting "Work files", you can ask Aira straight away to find the expense claim in it. When you choose read-write mode and the document provider allows writing, Aira can also create, replace or append UTF-8 text. Tapping "Revoke access" takes back access to the folder; copies already imported into the workspace stay usable.

Folders granted through the system are separate from the phone helper's experimental switch. The tools need a device that supports the files and computing environment, and a supervised task started by the user from the app.

## File tools

- `list_folder_files` without a path lists the folders the system has granted permanently. When the system can confirm the local storage location, the Settings page and the tool both show it. For example, "Download/Aira on internal storage" as the user says it can be matched with `/storage/emulated/0/Download/Aira` in `displayLocation`. The `/folders/…` ID of the same entry is used for later file actions. When only a display name is known and there are several possible targets, ask the user before changing anything. Passing a folder ID lists up to 200 direct children, and returns the writable state and a truncation flag.
- `search_device_files` searches recursively inside a given folder by file name, extension or modified time. A search can scan entries beyond the list preview. When depth, time taken, entries or matches reach a limit, the result states why it was cut short.
- `copy_device_file` copies an ordinary file as it is into `/workspace/inbox/` of the current workspace, and returns the actual file name and byte count. Each file can be up to 200 MiB, and a file with the same name gets a number added. Virtual documents must first be exported as ordinary files by their provider.
- `read_file`: accepts a `/folders/…` ID returned by the list, reads text page by page by bytes, and also returns the SHA-256 of the whole file.
- `write_file`: can create, replace or append text. Do not pass a version when creating; for an existing file you must pass the expectedSha256 returned by the last read, and nothing is written if the version does not match.
- `share_file`: saves a text file created in the workspace to the place given by folderPath; an existing target also needs expectedSha256. Without folderPath, the old Downloads folder and file card behaviour stays.

Scripts work on the `/workspace/…` copy returned after import. To read or write text directly in a granted folder, pass the file tools the `/folders/…` ID returned by the list. Each text file read or written in place can be up to 1 MiB.

## Implementation and limits

Access comes from Android's persistent URI permissions and is checked every time; UI actions, writes and revokes run one after another under the same process lock. A revoke waits for any write already started to finish, and later actions are refused. A search in a folder checks at most 2000 items, writing is refused when file names repeat, and the target is never picked by guessing a path.

Before an update, the version of the original is checked, and after writing it is read back to verify. Android document providers have no general atomic replace API: if a write fails, Aira tries to restore the old content or delete the partial file it just created; if restoring fails, it clearly reports that the file may have changed and never falsely reports success. The hash check can catch changes made after reading, but cannot stop another app from editing the same file between the last check and the write.

Import uses streaming copy, and reuses the space check and failure clean-up of workspace attachments. Access time for a remote document provider depends on how fast that provider responds.

## Verification

Local tests cover tool routing, paging of Chinese text, version arguments, export branches, path checks, size limits, and that remote or unattended tasks have no access. Existing workspace tools, folder grants, and resource and prompt checks are run again as regression.

`GrantedFolderFilesDeviceTest` uses the real system picker and a dedicated AiraFolderTest645 folder, and covers persistence of access, read-only access, version checks and export for Markdown, searching files beyond the list preview, importing a binary file larger than 1 MiB, and revoking access within the same task. Before running, create the empty dedicated folder; during the run, pick it in the system picker; afterwards, clean up the test files.
