package net.sf.sveditor.ui.argfile.editor.outline;

import net.sf.sveditor.core.SVFileUtils;
import net.sf.sveditor.core.Tuple;
import net.sf.sveditor.core.db.ISVDBItemBase;
import net.sf.sveditor.core.db.SVDBFileTree;
import net.sf.sveditor.core.db.index.SVDBFilePath;
import net.sf.sveditor.ui.SVUiPlugin;
import net.sf.sveditor.ui.svcp.SVDBDecoratingLabelProvider;
import net.sf.sveditor.ui.svcp.SVTreeLabelProvider;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class SVArgFileOutlineLabelProvider extends LabelProvider {
	private SVDBDecoratingLabelProvider			fBaseLabelProvider;
	
	public SVArgFileOutlineLabelProvider() {
		fBaseLabelProvider = new SVDBDecoratingLabelProvider(
				new SVTreeLabelProvider());
		
	}

	@Override
	@SuppressWarnings("unchecked")
	public Image getImage(Object element) {
		if (element instanceof SVDBFilePath) {
			return SVUiPlugin.getImage("/icons/eview16/include_hi.png");
		} else if (element instanceof Tuple) {
			Tuple<SVDBFileTree, ISVDBItemBase> t = (Tuple<SVDBFileTree, ISVDBItemBase>)element;
			
			if (t.second() != null) {
				ISVDBItemBase it = t.second();

				return fBaseLabelProvider.getImage(it);
			} else {
				// root file
				return SVUiPlugin.getImage("/icons/eview16/configs.gif");
			}
		} else {
			return fBaseLabelProvider.getImage(element);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public String getText(Object element) {
		if (element instanceof SVDBFilePath) {
			return "Include Hierarchy";
		} else if (element instanceof Tuple) {
			Tuple<SVDBFileTree, ISVDBItemBase> it = (Tuple<SVDBFileTree, ISVDBItemBase>)element;
			String leaf = SVFileUtils.getPathLeaf(it.first().getFilePath());
			
			if (it.second() == null) {
				return "Active File (" + leaf + ")";
			} else {
				return leaf;
			}
		} else {
			return fBaseLabelProvider.getText(element);
		}
	}
	
}
