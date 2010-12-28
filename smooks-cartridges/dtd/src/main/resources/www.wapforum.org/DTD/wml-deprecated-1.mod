<!-- WML Deprecated Module  .................................... -->
<!-- file: wml-deprecated-1.mod -->
<!-- 
    @Wireless Application Protocol Forum, Ltd. 2001.

	Terms and conditions of use are available from the Wireless Application Protocol Forum Ltd. 
	Web site (http://www.wapforum.org/what/copyright.htm).
-->
<!--
     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//WAPFORUM//ELEMENTS WML Deprecated 1.0//EN"
       SYSTEM "wml-deprecated-1.mod"

	This module contains deprecated elements and attributes.
	
		Attributes: WML mode, XHTML align, XHTML vspace, XHTML hspace
		
		Elements: u, b, big, i, small 
		
-->
<!-- 
	p 
	
	*WML mode
	*XHTML align
	
	Replacement: Use CSS style sheets 
-->
<!ATTLIST %p.qname;
	%att.mode.qname; (wrap | nowrap) #IMPLIED
	align (left | right | center) "left"
>
<!-- 
	table 
	
	*WML align 
	
	Note: The WML align attribute is not the same as the XHTML attribute with the same name.
-->
<!ATTLIST %table.qname;
	%att.align.qname; CDATA #IMPLIED
>
<!-- 
	img 
	
	*XHTML vspace, hspace, align
	
	Replacement: Use CSS style sheets 
-->
<!ATTLIST img
	vspace CDATA "0"
	hspace CDATA "0"
	align (top | middle | bottom) "bottom"
>
<!-- 
	Inline Presentation 
	
	*XHTML  b, big, i, small	
	
	Replacement: Use CSS style sheets 
-->
<!ENTITY % sub.element "IGNORE">
<!ENTITY % sub.attlist "IGNORE">
<!ENTITY % sup.element "IGNORE">
<!ENTITY % sup.attlist "IGNORE">
<!ENTITY % tt.element "IGNORE">
<!ENTITY % tt.attlist "IGNORE">
<!ENTITY % xhtml-inlpres.mod PUBLIC "-//W3C//ELEMENTS XHTML Inline Presentation 1.0//EN"
		"xhtml-inlpres-1.mod">
%xhtml-inlpres.mod;
<!-- 
	u

	This element is not available in the XHTML inline presentation module.
	It was deprecated already in HTML 4.0.
	
	Replacement: Use CSS style sheets 
-->
<!ENTITY % u.content "( #PCDATA | %Inline.mix; )*">
<!ENTITY % u.qname "u">
<!ELEMENT %u.qname; %u.content;>
<!ATTLIST %u.qname;
	%Common.attrib; 
>
<!-- 
	meta 
	
	*WML forua attribute
-->
<!ATTLIST %meta.qname;
	%att.forua.qname; %Boolean.datatype; "true"
>
<!--
	html
	
	*WML use-xml-fragment attribute
-->
<!ATTLIST %html.qname;
	%att.usexmlfragment.qname; 	%Boolean.datatype; "true"
>


