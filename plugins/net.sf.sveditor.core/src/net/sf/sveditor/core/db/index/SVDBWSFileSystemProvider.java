package net.sf.sveditor.core.db.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

public class SVDBWSFileSystemProvider implements ISVDBFileSystemProvider, 
		IResourceChangeListener, IResourceDeltaVisitor {
	
	private List<ISVDBFileSystemChangeListener>			fChangeListeners;
	private String										fRootDir;
	
	public SVDBWSFileSystemProvider() {
		fChangeListeners = new ArrayList<ISVDBFileSystemChangeListener>();
	}
	
	public void init(String path) {
		IFile 		file;
		IContainer 	folder;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		
		if (path.startsWith("${workspace_loc}")) {
			path = path.substring("${workspace_loc}".length());
		}
		
		folder = root.getFolder(new Path(path));
		
		if (!folder.exists()) {
			file = root.getFile(new Path(path));
			folder = file.getParent();
		}
		
		if (folder != null) {
			fRootDir = "${workspace_loc}" + folder.getFullPath().toOSString();
			try {
				folder.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) { }
		}
		
		System.out.println("ROOT_DIR=" + fRootDir);
		
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);		
	}
	
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);		
	}
	
	public boolean fileExists(String path) {
		if (path.startsWith("${workspace_loc}")) {
			path = path.substring("${workspace_loc}".length());
			
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
			IFile file = root.getFile(new Path(path));
			
			return file.exists();
		} else {
			// Also look at the filesystem
			return new File(path).exists();
		}
	}

	public void closeStream(InputStream in) {
		try {
			in.close();
		} catch (IOException e) { }
	}

	public InputStream openStream(String path) {
		InputStream ret = null;
		
		if (path.startsWith("${workspace_loc}")) {
			path = path.substring("${workspace_loc}".length());
			
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
			IFile file = root.getFile(new Path(path));
			
			try {
				ret = file.getContents();
			} catch (CoreException e) { 
				e.printStackTrace();
			}
		} else {
			try {
				ret = new FileInputStream(path);
			} catch (IOException e) {}
		}
		
		return ret;
	}

	public long getLastModifiedTime(String path) {
		if (path.startsWith("${workspace_loc}")) {
			path = path.substring("${workspace_loc}".length());
			
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			
			IFile file = root.getFile(new Path(path));
			
			if (file != null) {
				return file.getModificationStamp();
			} else {
				return 0;
			}
		} else {
			return new File(path).lastModified();
		}
	}

	public void addFileSystemChangeListener(ISVDBFileSystemChangeListener l) {
		fChangeListeners.add(l);
	}

	public void removeFileSystemChangeListener(ISVDBFileSystemChangeListener l) {
		fChangeListeners.remove(l);
	}

	public synchronized boolean visit(IResourceDelta delta) throws CoreException {
		
		if (delta.getResource() instanceof IFile) {
			String file = "${workspace_loc}";
			
			file += ((IFile)delta.getResource()).getFullPath().toOSString();
			
			System.out.println("visit: file=" + file);
			
			if (file.startsWith(fRootDir)) {
				if (delta.getKind() == IResourceDelta.REMOVED) {
					// remove from the queue (if present) and the index
					for (ISVDBFileSystemChangeListener l : fChangeListeners) {
						l.fileRemoved(file);
					}
				} else if (delta.getKind() == IResourceDelta.ADDED) {
					for (ISVDBFileSystemChangeListener l : fChangeListeners) {
						l.fileAdded(file);
					}
				} else {
					for (ISVDBFileSystemChangeListener l : fChangeListeners) {
						l.fileChanged(file);
					}
				}
			}
		}

		return true;
	}

	public void resourceChanged(IResourceChangeEvent event) {
		try {
			if (event.getDelta() != null) {
				event.getDelta().accept(this);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
}
