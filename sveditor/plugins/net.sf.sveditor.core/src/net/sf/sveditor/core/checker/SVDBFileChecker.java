package net.sf.sveditor.core.checker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.sveditor.core.db.ISVDBChildParent;
import net.sf.sveditor.core.db.ISVDBItemBase;
import net.sf.sveditor.core.db.ISVDBScopeItem;
import net.sf.sveditor.core.db.SVDBItemType;
import net.sf.sveditor.core.db.SVDBLocation;
import net.sf.sveditor.core.db.index.ISVDBMarkerMgr;
import net.sf.sveditor.core.preproc.ISVPreProcFileMapper;

public class SVDBFileChecker implements ISVDBChecker, ISVDBCheckErrorReporter {
	private ISVDBMarkerMgr								fMarkerMgr;
	private ISVPreProcFileMapper						fMapper;
	private Map<SVDBItemType, List<ISVDBCheckVisitor>> 	fCheckers;
	
	public SVDBFileChecker(ISVDBMarkerMgr marker_mgr, ISVPreProcFileMapper mapper) {
		fMarkerMgr = marker_mgr;
		fMapper = mapper;
		fCheckers = new HashMap<SVDBItemType, List<ISVDBCheckVisitor>>();
	}
	
	public void addChecker(SVDBItemType t, ISVDBCheckVisitor v) {
		if (!fCheckers.containsKey(t)) {
			fCheckers.put(t, new ArrayList<ISVDBCheckVisitor>());
		}
		List<ISVDBCheckVisitor> visitors = fCheckers.get(t);
	
		if (!visitors.contains(v)) {
			visitors.add(v);
		}
	}
	
	public void check(ISVDBScopeItem scope) {
		List<ISVDBCheckVisitor> v_l;
		
		if ((v_l = fCheckers.get(scope.getType())) != null) {
			for (ISVDBCheckVisitor v : v_l) {
				v.visit(this, scope);
			}
		}
		
		// Now, traverse through the sub-scopes
		for (ISVDBItemBase it : scope.getChildren()) {
			check(it);
		}
	}
	
	private void check(ISVDBItemBase it) {
		List<ISVDBCheckVisitor> v_l;
		
		if ((v_l = fCheckers.get(it.getType())) != null) {
			for (ISVDBCheckVisitor v : v_l) {
				v.visit(this, it);
			}
		}

		if (it instanceof ISVDBChildParent) {
			for (ISVDBItemBase it_c : ((ISVDBChildParent)it).getChildren()) {
				check(it_c);
			}
		}
	}

	@Override
	public void error(SVDBLocation loc, String msg) {
		String file = null;
		if (fMapper != null && loc != null) {
			file = fMapper.mapFileIdToPath(loc.getFileId());
		}
		
		if (file != null) {
			fMarkerMgr.addMarker(file, 
					ISVDBMarkerMgr.MARKER_TYPE_ERROR, loc.getLine(), msg);
		}
	}
	
}
