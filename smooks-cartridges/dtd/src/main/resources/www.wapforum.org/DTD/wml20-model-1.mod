<!-- WML 2.0 Document Model Module  .................................... -->
<!-- file: wml20-model-1.mod -->
<!-- 
    @Wireless Application Protocol Forum, Ltd. 2001.

	Terms and conditions of use are available from the Wireless Application Protocol Forum Ltd. 
	Web site (http://www.wapforum.org/what/copyright.htm).
-->
<!--
     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//WAPFORUM//ENTITIES WML 2.0 Document Model 1.0//EN"
       SYSTEM "http://www.wapforum.org/dtd/wml20-model-1.mod"
      
     This module describes the groupings of elements that make up
     common content models for WML and XHTML elements.

     Three basic content models are declared:

         %Inline.mix;  character-level elements
         %Block.mix;   block-like elements, eg., paragraphs and lists
         %Flow.mix;    any block or inline elements

     Any parameter entities declared in this module may be used
     to create element content models, but the above three are
     considered 'global' (insofar as that term applies here).
     
        	     
-->
<!-- 
	Optional Elements in head  
-->
<!ENTITY % HeadOpts.mix "( %meta.qname; 
		| %link.qname; 
		| %style.qname; 
		| %object.qname; 
		| %access.qname;)*">
<!-- 
	Task elements

	Used for the WML do and onevent elements
-->
<!ENTITY % Tasks.class "(%go.qname; 
		| %prev.qname; 
		| %noop.qname; 
		| %refresh.qname;)">
<!-- 
	Tasks without noop element

	Used for the WML anchor
-->
<!ENTITY % Tasks-nonoop.class "%go.qname; 
		| %prev.qname; 
		| %refresh.qname;">
<!--
	Do element
-->
<!ENTITY % Do.class "%do.qname;">
<!-- 
	Miscellaneous Elements  
	
	This is not used for anything right now. 
-->
<!ENTITY % Misc.class "">
<!-- 
	Inline Elements  
-->
<!ENTITY % InlStruct.class "%br.qname; 
	| %span.qname;">
<!ENTITY % InlPhras.class "| %em.qname; 
	| %strong.qname; 
	| %dfn.qname; 
	| %code.qname; 
	| %samp.qname; 
	| %kbd.qname; 
	| %var.qname; 
	| %cite.qname; 
     	| %abbr.qname; 
	| %acronym.qname; 
	| %q.qname;">
<!ENTITY % InlPres.class "| %i.qname; 
	| %b.qname; 
	| %big.qname; 
	| %small.qname;
	| %u.qname;">
<!ENTITY % I18n.class "">
<!ENTITY % Anchor.class "| %a.qname; 
	| %anchor.qname; | %do.qname;">
<!ENTITY % InlSpecial.class "| %img.qname; 
	 | %object.qname; 
	| %getvar.qname;">
<!ENTITY % InlForm.class "| %input.qname; 
	 | %select.qname; 
	 | %textarea.qname;
         | %label.qname;">
<!ENTITY % Inline.extra "">
<!ENTITY % Inline.class "%InlStruct.class;
      %InlPhras.class;
      %InlPres.class;
      %Anchor.class;
      %InlSpecial.class;
      %InlForm.class;
      %Inline.extra;">
<!ENTITY % InlNoAnchor.class "%InlStruct.class;
      %InlPhras.class;
      %InlPres.class;
      %InlSpecial.class;
      %InlForm.class;
      %Inline.extra;">
<!ENTITY % InlNoAnchor.mix "%InlNoAnchor.class;
      %Misc.class;">
<!ENTITY % Inline.mix "%Inline.class;
      %Misc.class;">
<!-- 
	Block Elements  
-->
<!ENTITY % Heading.class "%h1.qname; 
	| %h2.qname; 
	| %h3.qname; 
	| %h4.qname; 
	| %h5.qname; 
	| %h6.qname;">
<!ENTITY % List.class "%ul.qname; 
	| %ol.qname; 
	| %dl.qname;">
<!ENTITY % Table.class "| %table.qname;">
<!ENTITY % Form.class "| %form.qname;">
<!ENTITY % Fieldset.class "| %fieldset.qname;">
<!ENTITY % BlkStruct.class "%p.qname; 
	| %div.qname;">
<!ENTITY % BlkPhras.class "| %pre.qname; 
	 | %blockquote.qname; 
	 | %address.qname;">
<!ENTITY % BlkPres.class "| %hr.qname;">
<!ENTITY % BlkSpecial.class "%Table.class;
      %Form.class;
      %Fieldset.class;">
<!ENTITY % Block.extra "">
<!ENTITY % Block.class "%BlkStruct.class;
      %BlkPhras.class;
	 %BlkPres.class;
      %BlkSpecial.class;
      %Block.extra;">
<!ENTITY % Block.mix "%Heading.class;
      | %List.class;
      | %Block.class;     
      | %Do.class;
      %Misc.class;">
<!ENTITY % BlkNoForm.mix "%Heading.class;
      | %List.class;
      | %BlkStruct.class;
      %BlkPhras.class;
      %BlkPres.class;
      | %table.qname;
      %Block.extra;
      %Misc.class;">
<!-- 
	All Content Elements  
-->
<!-- 
	declares all content except tables
-->
<!ENTITY % FlowNoTable.mix "%Heading.class;
      | %List.class;
      | %BlkStruct.class;
      %BlkPhras.class;
      %Form.class;
      %Block.extra;
      | %Inline.class;
      %Misc.class;">
<!ENTITY % Flow.mix "%Heading.class;
      | %List.class;
      | %Block.class;
      | %Inline.class;
      %Misc.class;">
<!-- 	
	Template level elements 
-->
<!ENTITY % Template.mix "( %onevent.qname;)*">
<!-- 
	Content model of the WML card and XHTML body elements
-->
<!ENTITY % CardBody.mix "(%onevent.qname;*, %timer.qname;?,  ( %Block.mix; )+)">
<!-- end of wml20-model-1.mod -->
