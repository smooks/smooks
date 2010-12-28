<!-- WML Qualified Names Module  ......................................... -->
<!-- file: wml-qname-1.mod -->
<!-- 
    @Wireless Application Protocol Forum, Ltd. 2001.

	Terms and conditions of use are available from the Wireless Application Protocol Forum Ltd. 
	Web site (http://www.wapforum.org/what/copyright.htm).
-->
<!--
    
     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

	PUBLIC "-//WAPFORUM//ENTITIES WML Qualified Names 1.0//EN"
       SYSTEM "wml-qname-1.mod"

	This module declares WML names with the WML namespace prefix.	

-->
<!--
	The WML namespace name
-->
<!ENTITY % WML.xmlns "http://www.wapforum.org/2001/wml">
<!--
	WML namespace prefix
	
	On WML elements WML is declared as the default namespace. 
	Namespace prefix is used only for WML attributes on XHTML elements.
-->
<!ENTITY % WML.prefix "wml">
<!--
	Parameter entity with the WML namespace prefix plus the ":". 
	The actual prefix, WML.prefix, is declared in the WML DTD driver. 
-->
<!ENTITY % WML.pfx "%WML.prefix;:">
<!--
	Attribute used to declare the WML namespace with the WML prefix.
-->
<!ENTITY % WML.xmlns.extra.attrib "xmlns:%WML.prefix;	 %URI.datatype; 		#FIXED 	'%WML.xmlns;'">
<!--
	All WML qualified names. 
	
	Note that element names are not prefixed. Only attribute names are.
-->
<!ENTITY % card.qname "%WML.pfx;card">
<!ENTITY % do.qname "%WML.pfx;do">
<!ENTITY % access.qname "%WML.pfx;access">
<!ENTITY % go.qname "%WML.pfx;go">
<!ENTITY % prev.qname "%WML.pfx;prev">
<!ENTITY % refresh.qname "%WML.pfx;refresh">
<!ENTITY % noop.qname "%WML.pfx;noop">
<!ENTITY % onevent.qname "%WML.pfx;onevent">
<!ENTITY % postfield.qname "%WML.pfx;postfield">
<!ENTITY % setvar.qname "%WML.pfx;setvar">
<!ENTITY % getvar.qname "%WML.pfx;getvar">
<!ENTITY % timer.qname "%WML.pfx;timer">
<!ENTITY % widget.qname "%WML.pfx;widget">
<!ENTITY % anchor.qname "%WML.pfx;anchor">
<!ENTITY % att.forua.qname "%WML.pfx;forua">
<!ENTITY % att.value.qname "%WML.pfx;value">
<!ENTITY % att.iname.qname "%WML.pfx;iname">
<!ENTITY % att.name.qname "%WML.pfx;name">
<!ENTITY % att.ivalue.qname "%WML.pfx;ivalue">
<!ENTITY % att.format.qname "%WML.pfx;format">
<!ENTITY % att.emptyok.qname "%WML.pfx;emptyok">
<!ENTITY % att.localsrc.qname "%WML.pfx;localsrc">
<!ENTITY % att.columns.qname "%WML.pfx;columns">
<!ENTITY % att.align.qname "%WML.pfx;align">
<!ENTITY % att.mode.qname "%WML.pfx;mode">
<!ENTITY % att.onenterforward.qname "%WML.pfx;onenterforward">
<!ENTITY % att.onenterbackward.qname "%WML.pfx;onenterbackward">
<!ENTITY % att.ontimer.qname "%WML.pfx;ontimer">
<!ENTITY % att.onpick.qname "%WML.pfx;onpick">
<!ENTITY % att.newcontext.qname "%WML.pfx;newcontext">
<!ENTITY % att.usexmlfragment.qname "%WML.pfx;use-xml-fragments">
<!ENTITY % att.type.qname "%WML.pfx;type">
<!--
	The u element is not declared in XHTML, because it has beed removed from the language. 
	So it must be declared here. It is, however, deprecated in WML. 
-->
<!ENTITY % u.qname "u">
