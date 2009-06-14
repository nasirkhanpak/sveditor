package net.sf.sveditor.core.scanner;


public interface IDefineProvider {
	
//	String getDefineVal(String key, List<String> params);
	
	String expandMacro(String string, String filename, int lineno);
	
	boolean isDefined(String name, int lineno);
	
	boolean hasParameters(String key);

}