<!-- WML Modular Framework ...................................................... -->
<!-- file: wml-framework-1.mod -->
<!-- 
    @Wireless Application Protocol Forum, Ltd. 2001.

	Terms and conditions of use are available from the Wireless Application Protocol Forum Ltd. 
	Web site (http://www.wapforum.org/what/copyright.htm).
-->
<!--
     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//WAPFORUM//ENTITIES WML Modular Framework 1.0//EN"
       SYSTEM "http://www.wapforum.org/dtd/wml-framework-1.mod"
       
       This is an extension of the XHTML Modular Framework module. In addition to the 
       required XHTML entities it includes:
       
       		*WML datatypes
       		*WML namespace qualified names
       		*WML document model       
-->
<!-- 
	XHTML notations
-->
<!ENTITY % xhtml-notations.mod PUBLIC "-//W3C//NOTATIONS XHTML Notations 1.0//EN"
            "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-notations-1.mod">
%xhtml-notations.mod;
<!-- 
	WML datatypes
-->
<!ENTITY % Boolean.datatype "(true|false)">
<!-- 
	XHTML datatypes
-->
<!ENTITY % xhtml-datatypes.mod PUBLIC "-//W3C//ENTITIES XHTML Datatypes 1.0//EN"
            "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-datatypes-1.mod">
%xhtml-datatypes.mod;
<!-- 
	WML qualified names
-->
<!ENTITY % wml-qname.mod PUBLIC "-//WAPFORUM//ENTITIES WML Qualified Names 1.0//EN"
       "http://www.wapforum.org/DTD/wml-qname-1.mod">
%wml-qname.mod;
<!-- 
	XHTML qualified names
-->
<!ENTITY % xhtml-qname.mod PUBLIC "-//W3C//ENTITIES XHTML Qualified Names 1.0//EN"
            "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-qname-1.mod">
%xhtml-qname.mod;
<!--
	XHTML global attributes
-->
<!ENTITY % xhtml-inlstyle.mod PUBLIC "-//W3C//ELEMENTS XHTML Inline Style 1.0//EN"
            "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-inlstyle-1.mod">
%xhtml-inlstyle.mod;
<!ENTITY % XHTML.bidi "IGNORE">
<!ENTITY % xhtml-attribs.mod PUBLIC "-//W3C//ENTITIES XHTML Common Attributes 1.0//EN"
            "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-attribs-1.mod">
%xhtml-attribs.mod;
<!-- 
	XHTML character entities
-->
<!ENTITY % xhtml-charent.mod PUBLIC "-//W3C//ENTITIES XHTML Character Entities 1.0//EN"
            "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-charent-1.mod">
%xhtml-charent.mod;
<!-- 
	WML document model
-->
<!ENTITY % wml-model.mod PUBLIC "-//WAPFORUM//ENTITIES WML 2.0 Document Model 1.0//EN"
       "http://www.wapforum.org/DTD/wml20-model-1.mod">
%wml-model.mod;
