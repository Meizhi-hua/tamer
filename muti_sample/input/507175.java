public class XSLTSchema extends XSLTElementDef
{
  XSLTSchema()
  {
    build();
  }
  void build()
  {
    XSLTAttributeDef hrefAttr = new XSLTAttributeDef(null, "href",
                                  XSLTAttributeDef.T_URL, true, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef elementsAttr = new XSLTAttributeDef(null, "elements",
                                      XSLTAttributeDef.T_SIMPLEPATTERNLIST,
                                      true, false, XSLTAttributeDef.ERROR);
    XSLTAttributeDef methodAttr = new XSLTAttributeDef(null, "method",
                                    XSLTAttributeDef.T_QNAME, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef versionAttr = new XSLTAttributeDef(null, "version",
                                     XSLTAttributeDef.T_NMTOKEN, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef encodingAttr = new XSLTAttributeDef(null, "encoding",
                                      XSLTAttributeDef.T_CDATA, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef omitXmlDeclarationAttr = new XSLTAttributeDef(null,
                                                "omit-xml-declaration",
                                                XSLTAttributeDef.T_YESNO,
                                                false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef standaloneAttr = new XSLTAttributeDef(null,
                                        "standalone",
                                        XSLTAttributeDef.T_YESNO, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef doctypePublicAttr = new XSLTAttributeDef(null,
                                           "doctype-public",
                                           XSLTAttributeDef.T_CDATA, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef doctypeSystemAttr = new XSLTAttributeDef(null,
                                           "doctype-system",
                                           XSLTAttributeDef.T_CDATA, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef cdataSectionElementsAttr = new XSLTAttributeDef(null,
                                                  "cdata-section-elements",
                                                  XSLTAttributeDef.T_QNAMES_RESOLVE_NULL,
                                                  false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef indentAttr = new XSLTAttributeDef(null, "indent",
                                    XSLTAttributeDef.T_YESNO, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef mediaTypeAttr = new XSLTAttributeDef(null, "media-type",
                                       XSLTAttributeDef.T_CDATA, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef nameAttrRequired = new XSLTAttributeDef(null, "name",
                                          XSLTAttributeDef.T_QNAME, true, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef nameAVTRequired = new XSLTAttributeDef(null, "name",
                                         XSLTAttributeDef.T_AVT_QNAME, true, true,XSLTAttributeDef.WARNING);
    XSLTAttributeDef nameAVT_NCNAMERequired = new XSLTAttributeDef(null, "name",
                                         XSLTAttributeDef.T_NCNAME, true, true,XSLTAttributeDef.WARNING);
    XSLTAttributeDef nameAttrOpt_ERROR = new XSLTAttributeDef(null, "name",
                                     XSLTAttributeDef.T_QNAME, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef useAttr = new XSLTAttributeDef(null, "use",
                                 XSLTAttributeDef.T_EXPR, true, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef namespaceAVTOpt = new XSLTAttributeDef(null,
                                         "namespace",XSLTAttributeDef.T_URL,
                                         false, true,XSLTAttributeDef.WARNING);
    XSLTAttributeDef decimalSeparatorAttr = new XSLTAttributeDef(null,
                                              "decimal-separator",
                                              XSLTAttributeDef.T_CHAR, false,XSLTAttributeDef.ERROR, ".");
    XSLTAttributeDef infinityAttr = new XSLTAttributeDef(null, "infinity",
                                      XSLTAttributeDef.T_CDATA, false,XSLTAttributeDef.ERROR,"Infinity");
    XSLTAttributeDef minusSignAttr = new XSLTAttributeDef(null, "minus-sign",
                                       XSLTAttributeDef.T_CHAR, false,XSLTAttributeDef.ERROR,"-");
    XSLTAttributeDef NaNAttr = new XSLTAttributeDef(null, "NaN",
                                 XSLTAttributeDef.T_CDATA, false,XSLTAttributeDef.ERROR, "NaN");
    XSLTAttributeDef percentAttr = new XSLTAttributeDef(null, "percent",
                                     XSLTAttributeDef.T_CHAR, false,XSLTAttributeDef.ERROR, "%");
    XSLTAttributeDef perMilleAttr = new XSLTAttributeDef(null, "per-mille",
                                      XSLTAttributeDef.T_CHAR,
                                      false, false,XSLTAttributeDef.ERROR );
    XSLTAttributeDef zeroDigitAttr = new XSLTAttributeDef(null, "zero-digit",
                                       XSLTAttributeDef.T_CHAR, false,XSLTAttributeDef.ERROR, "0");
    XSLTAttributeDef digitAttr = new XSLTAttributeDef(null, "digit",
                                   XSLTAttributeDef.T_CHAR, false,XSLTAttributeDef.ERROR, "#");
    XSLTAttributeDef patternSeparatorAttr = new XSLTAttributeDef(null,
                                              "pattern-separator",
                                              XSLTAttributeDef.T_CHAR, false,XSLTAttributeDef.ERROR, ";");
    XSLTAttributeDef groupingSeparatorAttr = new XSLTAttributeDef(null,
                                               "grouping-separator",
                                               XSLTAttributeDef.T_CHAR, false,XSLTAttributeDef.ERROR,",");
    XSLTAttributeDef useAttributeSetsAttr = new XSLTAttributeDef(null,
                                              "use-attribute-sets",
                                              XSLTAttributeDef.T_QNAMES,
                                              false, false, XSLTAttributeDef.ERROR);
    XSLTAttributeDef testAttrRequired = new XSLTAttributeDef(null, "test",   
                                          XSLTAttributeDef.T_EXPR, true, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef selectAttrRequired = new XSLTAttributeDef(null,
                                            "select",
                                            XSLTAttributeDef.T_EXPR, true, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef selectAttrOpt = new XSLTAttributeDef(null, "select",
                                       XSLTAttributeDef.T_EXPR, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef selectAttrDefNode = new XSLTAttributeDef(null, "select",
                                           XSLTAttributeDef.T_EXPR, false,XSLTAttributeDef.ERROR, "node()");
    XSLTAttributeDef selectAttrDefDot = new XSLTAttributeDef(null, "select",
                                          XSLTAttributeDef.T_EXPR, false,XSLTAttributeDef.ERROR, ".");
    XSLTAttributeDef matchAttrRequired = new XSLTAttributeDef(null, "match",
                                           XSLTAttributeDef.T_PATTERN, true, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef matchAttrOpt = new XSLTAttributeDef(null, "match",
                                      XSLTAttributeDef.T_PATTERN, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef priorityAttr = new XSLTAttributeDef(null, "priority",
                                     XSLTAttributeDef.T_NUMBER, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef modeAttr = new XSLTAttributeDef(null, "mode",
                                     XSLTAttributeDef.T_QNAME, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef spaceAttr =
      new XSLTAttributeDef(Constants.S_XMLNAMESPACEURI, "space", false, false, false, XSLTAttributeDef.WARNING,
                           "default", Constants.ATTRVAL_STRIP, "preserve",
                           Constants.ATTRVAL_PRESERVE);
    XSLTAttributeDef spaceAttrLiteral =
      new XSLTAttributeDef(Constants.S_XMLNAMESPACEURI, "space", 
                                          XSLTAttributeDef.T_URL, false, true,XSLTAttributeDef.ERROR);
    XSLTAttributeDef stylesheetPrefixAttr = new XSLTAttributeDef(null,
                                              "stylesheet-prefix",
                                              XSLTAttributeDef.T_CDATA, true, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef resultPrefixAttr = new XSLTAttributeDef(null,
                                          "result-prefix",
                                          XSLTAttributeDef.T_CDATA, true, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef disableOutputEscapingAttr = new XSLTAttributeDef(null,
                                                   "disable-output-escaping",
                                                   XSLTAttributeDef.T_YESNO,
                                                   false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef levelAttr = new XSLTAttributeDef(null, "level", false, false, false, XSLTAttributeDef.ERROR,
                                   "single", Constants.NUMBERLEVEL_SINGLE,
                                   "multiple", Constants.NUMBERLEVEL_MULTI,
                                   "any", Constants.NUMBERLEVEL_ANY);
    levelAttr.setDefault("single");
    XSLTAttributeDef countAttr = new XSLTAttributeDef(null, "count",
                                   XSLTAttributeDef.T_PATTERN, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef fromAttr = new XSLTAttributeDef(null, "from",
                                  XSLTAttributeDef.T_PATTERN, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef valueAttr = new XSLTAttributeDef(null, "value",
                                   XSLTAttributeDef.T_EXPR, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef formatAttr = new XSLTAttributeDef(null, "format",
                                    XSLTAttributeDef.T_CDATA, false, true,XSLTAttributeDef.ERROR);
    formatAttr.setDefault("1");
    XSLTAttributeDef langAttr = new XSLTAttributeDef(null, "lang",
                                  XSLTAttributeDef.T_NMTOKEN, false, true,XSLTAttributeDef.ERROR);
    XSLTAttributeDef letterValueAttr = new XSLTAttributeDef(null,
                                         "letter-value",
                                         false, true, false, XSLTAttributeDef.ERROR,
                                         "alphabetic", Constants.NUMBERLETTER_ALPHABETIC,
                                         "traditional", Constants.NUMBERLETTER_TRADITIONAL);
    XSLTAttributeDef groupingSeparatorAVT = new XSLTAttributeDef(null,
                                              "grouping-separator",
                                              XSLTAttributeDef.T_CHAR, false, true,XSLTAttributeDef.ERROR);
    XSLTAttributeDef groupingSizeAttr = new XSLTAttributeDef(null,
                                          "grouping-size",
                                          XSLTAttributeDef.T_NUMBER, false, true,XSLTAttributeDef.ERROR);
    XSLTAttributeDef dataTypeAttr = new XSLTAttributeDef(null, "data-type", false, true, true, XSLTAttributeDef.ERROR,
                                    "text", Constants.SORTDATATYPE_TEXT ,"number", Constants.SORTDATATYPE_TEXT);
	dataTypeAttr.setDefault("text");
    XSLTAttributeDef orderAttr = new XSLTAttributeDef(null, "order", false, true, false,XSLTAttributeDef.ERROR,
                                    "ascending", Constants.SORTORDER_ASCENDING, 
                                    "descending", Constants.SORTORDER_DESCENDING);
    orderAttr.setDefault("ascending");
    XSLTAttributeDef caseOrderAttr = new XSLTAttributeDef(null, "case-order", false, true, false,XSLTAttributeDef.ERROR,
                                       "upper-first", Constants.SORTCASEORDER_UPPERFIRST ,
                                       "lower-first", Constants.SORTCASEORDER_LOWERFIRST);
    XSLTAttributeDef terminateAttr = new XSLTAttributeDef(null, "terminate",
                                       XSLTAttributeDef.T_YESNO, false, false,XSLTAttributeDef.ERROR);
    terminateAttr.setDefault("no");
    XSLTAttributeDef xslExcludeResultPrefixesAttr =
      new XSLTAttributeDef(Constants.S_XSLNAMESPACEURL,
                           "exclude-result-prefixes",
                           XSLTAttributeDef.T_PREFIXLIST, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef xslExtensionElementPrefixesAttr =
      new XSLTAttributeDef(Constants.S_XSLNAMESPACEURL,
                           "extension-element-prefixes",
                           XSLTAttributeDef.T_PREFIX_URLLIST, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef xslUseAttributeSetsAttr =
      new XSLTAttributeDef(Constants.S_XSLNAMESPACEURL, "use-attribute-sets",
                           XSLTAttributeDef.T_QNAMES, false, false,XSLTAttributeDef.ERROR);
    XSLTAttributeDef xslVersionAttr =
      new XSLTAttributeDef(Constants.S_XSLNAMESPACEURL, "version",
                           XSLTAttributeDef.T_NMTOKEN, false, false,XSLTAttributeDef.ERROR);
    XSLTElementDef charData = new XSLTElementDef(this, null, "text()",
                                null , null , null,  
                                new ProcessorCharacters(),
                                ElemTextLiteral.class );
    charData.setType(XSLTElementDef.T_PCDATA);
    XSLTElementDef whiteSpaceOnly = new XSLTElementDef(this, null, "text()",
                                      null , null ,
                                      null,  
                                      null,
                                      ElemTextLiteral.class );
    charData.setType(XSLTElementDef.T_PCDATA);
    XSLTAttributeDef resultAttr = new XSLTAttributeDef(null, "*",
                                    XSLTAttributeDef.T_AVT, false, true,XSLTAttributeDef.WARNING);
    XSLTAttributeDef xslResultAttr =
      new XSLTAttributeDef(Constants.S_XSLNAMESPACEURL, "*",
                           XSLTAttributeDef.T_CDATA, false, false,XSLTAttributeDef.WARNING);
    XSLTElementDef[] templateElements = new XSLTElementDef[23];
    XSLTElementDef[] templateElementsAndParams = new XSLTElementDef[24];
    XSLTElementDef[] templateElementsAndSort = new XSLTElementDef[24];
    XSLTElementDef[] exsltFunctionElements = new XSLTElementDef[24];
    XSLTElementDef[] charTemplateElements = new XSLTElementDef[15];
    XSLTElementDef resultElement = new XSLTElementDef(this, null, "*",
                                     null ,
                                     templateElements ,
                                     new XSLTAttributeDef[]{
                                       spaceAttrLiteral, 
                                       xslExcludeResultPrefixesAttr,
                                       xslExtensionElementPrefixesAttr,
                                       xslUseAttributeSetsAttr,
                                       xslVersionAttr,
                                       xslResultAttr,
                                       resultAttr }, 
                                        new ProcessorLRE(),
                                     ElemLiteralResult.class , 20, true);
    XSLTElementDef unknownElement =
      new XSLTElementDef(this, "*", "unknown", null ,
                         templateElementsAndParams ,
                         new XSLTAttributeDef[]{ xslExcludeResultPrefixesAttr,
                                                 xslExtensionElementPrefixesAttr,
                                                 xslUseAttributeSetsAttr,
                                                 xslVersionAttr,
                                                 xslResultAttr,
                                                 resultAttr }, 
                                                                                                 new ProcessorUnknown(),
                         ElemUnknown.class , 20, true);
    XSLTElementDef xslValueOf = new XSLTElementDef(this,
                                  Constants.S_XSLNAMESPACEURL, "value-of",
                                  null , null ,
                                  new XSLTAttributeDef[]{ selectAttrRequired,
                                                          disableOutputEscapingAttr }, 
                                               new ProcessorTemplateElem(),
                                  ElemValueOf.class , 20, true);
    XSLTElementDef xslCopyOf = new XSLTElementDef(this,
                                 Constants.S_XSLNAMESPACEURL, "copy-of",
                                 null , null ,
                                 new XSLTAttributeDef[]{ selectAttrRequired },
                                 new ProcessorTemplateElem(),
                                 ElemCopyOf.class , 20, true);
    XSLTElementDef xslNumber = new XSLTElementDef(this,
                                 Constants.S_XSLNAMESPACEURL, "number",
                                 null , null ,
                                 new XSLTAttributeDef[]{ levelAttr,
                                                         countAttr,
                                                         fromAttr,
                                                         valueAttr,
                                                         formatAttr,
                                                         langAttr,
                                                         letterValueAttr,
                                                         groupingSeparatorAVT,
                                                         groupingSizeAttr }, 
                                        new ProcessorTemplateElem(),
                                 ElemNumber.class , 20, true);
    XSLTElementDef xslSort = new XSLTElementDef(this,
                                                Constants.S_XSLNAMESPACEURL,
                                                "sort", null ,
                                                null ,
                                                new XSLTAttributeDef[]{
                                                  selectAttrDefDot,
                                                  langAttr,
                                                  dataTypeAttr,
                                                  orderAttr,
                                                  caseOrderAttr }, 
                                       new ProcessorTemplateElem(),
                                                ElemSort.class, 19, true );
    XSLTElementDef xslWithParam = new XSLTElementDef(this,
                                    Constants.S_XSLNAMESPACEURL,
                                    "with-param", null ,
                                    templateElements ,  
                                    new XSLTAttributeDef[]{ nameAttrRequired,
                                                            selectAttrOpt }, new ProcessorTemplateElem(),
                                                                             ElemWithParam.class , 19, true);
    XSLTElementDef xslApplyTemplates = new XSLTElementDef(this,
                                         Constants.S_XSLNAMESPACEURL,
                                         "apply-templates", null ,
                                         new XSLTElementDef[]{ xslSort,
                                                               xslWithParam } , new XSLTAttributeDef[]{
                                                                 selectAttrDefNode,
                                                                 modeAttr }, 
                                                                        new ProcessorTemplateElem(),
                                         ElemApplyTemplates.class , 20, true);
    XSLTElementDef xslApplyImports =
      new XSLTElementDef(this, Constants.S_XSLNAMESPACEURL, "apply-imports",
                         null , null ,
                         new XSLTAttributeDef[]{},
                         new ProcessorTemplateElem(),
                         ElemApplyImport.class );
    XSLTElementDef xslForEach = new XSLTElementDef(this,
                                  Constants.S_XSLNAMESPACEURL, "for-each",
                                  null , templateElementsAndSort,  
                                  new XSLTAttributeDef[]{ selectAttrRequired,
                                                          spaceAttr }, 
                                               new ProcessorTemplateElem(),
                                  ElemForEach.class , true, false, true, 20, true);
    XSLTElementDef xslIf = new XSLTElementDef(this,
                                              Constants.S_XSLNAMESPACEURL,
                                              "if", null ,
                                              templateElements ,  
                                              new XSLTAttributeDef[]{
                                                testAttrRequired,
                                                spaceAttr }, new ProcessorTemplateElem(),
                                                             ElemIf.class , 20, true);
    XSLTElementDef xslWhen =
      new XSLTElementDef(this, Constants.S_XSLNAMESPACEURL, "when",
                         null , templateElements ,  
                                                new XSLTAttributeDef[]{
                                                  testAttrRequired,
                                                  spaceAttr }, new ProcessorTemplateElem(),
                                                               ElemWhen.class ,
                                                                                                false, true, 1, true);
    XSLTElementDef xslOtherwise = new XSLTElementDef(this,
                                    Constants.S_XSLNAMESPACEURL, "otherwise",
                                    null ,
                                    templateElements ,  
                                    new XSLTAttributeDef[]{ spaceAttr },
                                    new ProcessorTemplateElem(),
                                    ElemOtherwise.class ,
                                                       false, false, 2, false);
    XSLTElementDef xslChoose = new XSLTElementDef(this,
                                 Constants.S_XSLNAMESPACEURL, "choose",
                                 null ,
                                 new XSLTElementDef[]{ xslWhen,
                                                       xslOtherwise } , 
                                        new XSLTAttributeDef[]{ spaceAttr },
                                 new ProcessorTemplateElem(),
                                 ElemChoose.class , true, false, true, 20, true);                                
    XSLTElementDef xslAttribute = new XSLTElementDef(this,
                                    Constants.S_XSLNAMESPACEURL, "attribute",
                                    null ,
                                    charTemplateElements ,  
                                    new XSLTAttributeDef[]{ nameAVTRequired,
                                                            namespaceAVTOpt,
                                                            spaceAttr }, 
                                    new ProcessorTemplateElem(),
                                    ElemAttribute.class , 20, true);
    XSLTElementDef xslCallTemplate =
      new XSLTElementDef(this, Constants.S_XSLNAMESPACEURL, "call-template",
                         null ,
                         new XSLTElementDef[]{ xslWithParam } ,
                         new XSLTAttributeDef[]{ nameAttrRequired },
                         new ProcessorTemplateElem(),
                         ElemCallTemplate.class , 20, true);
    XSLTElementDef xslVariable = new XSLTElementDef(this,
                                   Constants.S_XSLNAMESPACEURL, "variable",
                                   null ,
                                   templateElements ,  
                                   new XSLTAttributeDef[]{ nameAttrRequired,
                                                           selectAttrOpt }, 
                                  new ProcessorTemplateElem(),
                                   ElemVariable.class , 20, true);
    XSLTElementDef xslParam = new XSLTElementDef(this,
                                Constants.S_XSLNAMESPACEURL, "param",
                                null ,
                                templateElements ,  
                                new XSLTAttributeDef[]{ nameAttrRequired,
                                                        selectAttrOpt }, 
                                       new ProcessorTemplateElem(),
                                ElemParam.class , 19, true);
    XSLTElementDef xslText =
      new XSLTElementDef(this, Constants.S_XSLNAMESPACEURL, "text",
                         null ,
                         new XSLTElementDef[]{ charData } ,
                         new XSLTAttributeDef[]{ disableOutputEscapingAttr },
                         new ProcessorText(),
                         ElemText.class , 20, true);
    XSLTElementDef xslProcessingInstruction =
      new XSLTElementDef(this, Constants.S_XSLNAMESPACEURL,
                         "processing-instruction", null ,
                         charTemplateElements ,  
                         new XSLTAttributeDef[]{
                                                  nameAVT_NCNAMERequired,
                                                  spaceAttr }, 
                                        new ProcessorTemplateElem(),
                          ElemPI.class , 20, true);
    XSLTElementDef xslElement = new XSLTElementDef(this,
                                  Constants.S_XSLNAMESPACEURL, "element",
                                  null ,
                                  templateElements ,  
                                  new XSLTAttributeDef[]{ nameAVTRequired,
                                                          namespaceAVTOpt,
                                                          useAttributeSetsAttr,
                                                          spaceAttr }, 
                                               new ProcessorTemplateElem(),
                                  ElemElement.class , 20, true);
    XSLTElementDef xslComment = new XSLTElementDef(this,
                                  Constants.S_XSLNAMESPACEURL, "comment",
                                  null ,
                                  charTemplateElements ,  
                                  new XSLTAttributeDef[]{ spaceAttr },
                                  new ProcessorTemplateElem(),
                                  ElemComment.class , 20, true);
    XSLTElementDef xslCopy =
      new XSLTElementDef(this, Constants.S_XSLNAMESPACEURL, "copy",
                         null , templateElements ,  
                          new XSLTAttributeDef[]{
                                                  spaceAttr,
                                                  useAttributeSetsAttr }, 
                                        new ProcessorTemplateElem(),
                          ElemCopy.class , 20, true);
    XSLTElementDef xslMessage = new XSLTElementDef(this,
                                  Constants.S_XSLNAMESPACEURL, "message",
                                  null ,
                                  templateElements ,  
                                  new XSLTAttributeDef[]{ terminateAttr },
                                  new ProcessorTemplateElem(),
                                  ElemMessage.class , 20, true);
    XSLTElementDef xslFallback = new XSLTElementDef(this,
                                   Constants.S_XSLNAMESPACEURL, "fallback",
                                   null ,
                                   templateElements ,  
                                   new XSLTAttributeDef[]{ spaceAttr },
                                   new ProcessorTemplateElem(),
                                   ElemFallback.class , 20, true);
    XSLTElementDef exsltFunction =
                                  new XSLTElementDef(this, 
                                  Constants.S_EXSLT_FUNCTIONS_URL, 
                                  "function",
                                  null ,
                                  exsltFunctionElements ,
                                  new XSLTAttributeDef[]{ nameAttrRequired },
                                  new ProcessorExsltFunction(),
                                  ElemExsltFunction.class );
    XSLTElementDef exsltResult =
                                  new XSLTElementDef(this, 
                                  Constants.S_EXSLT_FUNCTIONS_URL, 
                                  "result",
                                  null ,
                                  templateElements ,
                                  new XSLTAttributeDef[]{ selectAttrOpt },
                                  new ProcessorExsltFuncResult(),
                                  ElemExsltFuncResult.class  );            
    int i = 0;
    templateElements[i++] = charData;  
    templateElements[i++] = xslApplyTemplates;
    templateElements[i++] = xslCallTemplate;
    templateElements[i++] = xslApplyImports;
    templateElements[i++] = xslForEach;
    templateElements[i++] = xslValueOf;
    templateElements[i++] = xslCopyOf;
    templateElements[i++] = xslNumber;
    templateElements[i++] = xslChoose;
    templateElements[i++] = xslIf;
    templateElements[i++] = xslText;
    templateElements[i++] = xslCopy;
    templateElements[i++] = xslVariable;
    templateElements[i++] = xslMessage;
    templateElements[i++] = xslFallback;
    templateElements[i++] = xslProcessingInstruction;
    templateElements[i++] = xslComment;
    templateElements[i++] = xslElement;
    templateElements[i++] = xslAttribute;
    templateElements[i++] = resultElement;
    templateElements[i++] = unknownElement;
    templateElements[i++] = exsltFunction;
    templateElements[i++] = exsltResult;
    System.arraycopy(templateElements, 0, templateElementsAndParams, 0, i);
    System.arraycopy(templateElements, 0, templateElementsAndSort, 0, i);
    System.arraycopy(templateElements, 0, exsltFunctionElements, 0, i);
    templateElementsAndParams[i] = xslParam;
    templateElementsAndSort[i] = xslSort;
    exsltFunctionElements[i]   = xslParam;
    i = 0;
    charTemplateElements[i++] = charData;  
    charTemplateElements[i++] = xslApplyTemplates;
    charTemplateElements[i++] = xslCallTemplate;
    charTemplateElements[i++] = xslApplyImports;
    charTemplateElements[i++] = xslForEach;
    charTemplateElements[i++] = xslValueOf;
    charTemplateElements[i++] = xslCopyOf;
    charTemplateElements[i++] = xslNumber;
    charTemplateElements[i++] = xslChoose;
    charTemplateElements[i++] = xslIf;
    charTemplateElements[i++] = xslText;
    charTemplateElements[i++] = xslCopy;
    charTemplateElements[i++] = xslVariable;
    charTemplateElements[i++] = xslMessage;
    charTemplateElements[i++] = xslFallback;
    XSLTElementDef importDef = new XSLTElementDef(this,
                                 Constants.S_XSLNAMESPACEURL, "import",
                                 null , null ,
                                 new XSLTAttributeDef[]{ hrefAttr },  
                                 new ProcessorImport(),
                                 null ,
                                        1, true);
    XSLTElementDef includeDef = new XSLTElementDef(this,
                                  Constants.S_XSLNAMESPACEURL, "include",
                                  null , null ,  
                                  new XSLTAttributeDef[]{ hrefAttr },
                                  new ProcessorInclude(),
                                  null ,
                                               20, true);
    XSLTAttributeDef[] scriptAttrs = new XSLTAttributeDef[]{
    					    new XSLTAttributeDef(null, "lang", XSLTAttributeDef.T_NMTOKEN,
                                                                 true, false,XSLTAttributeDef.WARNING),
                                            new XSLTAttributeDef(null, "src", XSLTAttributeDef.T_URL, 
                                            			 false, false,XSLTAttributeDef.WARNING)};
    XSLTAttributeDef[] componentAttrs = new XSLTAttributeDef[]{ 
                                            new XSLTAttributeDef(null, "prefix", XSLTAttributeDef.T_NMTOKEN, 
                                            			 true, false,XSLTAttributeDef.WARNING),
                                            new XSLTAttributeDef(null, "elements", XSLTAttributeDef.T_STRINGLIST, 
                                            			 false, false,XSLTAttributeDef.WARNING),
                                            new XSLTAttributeDef(null, "functions", XSLTAttributeDef.T_STRINGLIST, 
                                            			 false, false,XSLTAttributeDef.WARNING) };
    XSLTElementDef[] topLevelElements = new XSLTElementDef[]
                                 {includeDef,
                                  importDef,
                                  whiteSpaceOnly,
                                  unknownElement,
                                  new XSLTElementDef(
                                         this,
                                         Constants.S_XSLNAMESPACEURL,
                                         "strip-space",
                                         null ,
                                         null ,
                                         new XSLTAttributeDef[]{
                                                elementsAttr },
                                                new ProcessorStripSpace(),
                                         null , 20, true),
                                  new XSLTElementDef(
                                         this,
                                         Constants.S_XSLNAMESPACEURL,
                                         "preserve-space",
                                         null ,
                                         null ,
                                         new XSLTAttributeDef[]{
                                                 elementsAttr },
                                                 new ProcessorPreserveSpace(),
                                         null , 20, true),
                                  new XSLTElementDef(
                                         this,
                                         Constants.S_XSLNAMESPACEURL,
                                         "output",
                                         null ,
                                         null ,
                                         new XSLTAttributeDef[]{
                                                  methodAttr,
                                                  versionAttr,
                                                  encodingAttr,
                                                  omitXmlDeclarationAttr,
                                                  standaloneAttr,
                                                  doctypePublicAttr,
                                                  doctypeSystemAttr,
                                                  cdataSectionElementsAttr,
                                                  indentAttr,
                                                  mediaTypeAttr,
                                                  XSLTAttributeDef.m_foreignAttr }, 
                                          new ProcessorOutputElem(), null , 20, true), 
                                  new XSLTElementDef(
                                          this,
                                          Constants.S_XSLNAMESPACEURL,
                                          "key",
                                          null ,
                                          null ,  
                                          new XSLTAttributeDef[]{ nameAttrRequired,
                                                  matchAttrRequired,
                                                  useAttr }, 
                                          new ProcessorKey(), null , 20, true),
                                  new XSLTElementDef(
                                          this,
                                          Constants.S_XSLNAMESPACEURL,
                                          "decimal-format",
                                          null ,
                                          null ,  
                                          new XSLTAttributeDef[]{
                                                  nameAttrOpt_ERROR,
                                                  decimalSeparatorAttr,
                                                  groupingSeparatorAttr,
                                                  infinityAttr,
                                                  minusSignAttr,
                                                  NaNAttr,
                                                  percentAttr,
                                                  perMilleAttr,
                                                  zeroDigitAttr,
                                                  digitAttr,
                                                  patternSeparatorAttr }, 
                                           new ProcessorDecimalFormat(),
                                           null , 20, true),
                                  new XSLTElementDef(
                                           this,
                                           Constants.S_XSLNAMESPACEURL,
                                           "attribute-set",
                                           null ,
                                           new XSLTElementDef[]{
                                                   xslAttribute } ,
                                           new XSLTAttributeDef[]{
                                                   nameAttrRequired,
                                                   useAttributeSetsAttr }, 
                                           new ProcessorAttributeSet(),
                                           null , 20, true),
                                  new XSLTElementDef(
                                           this,
                                           Constants.S_XSLNAMESPACEURL,
                                           "variable",
                                           null ,
                                           templateElements ,
                                           new XSLTAttributeDef[]{
                                                   nameAttrRequired,
                                                   selectAttrOpt }, 
                                           new ProcessorGlobalVariableDecl(),
                                           ElemVariable.class , 20, true),
                                  new XSLTElementDef(
                                           this,
                                           Constants.S_XSLNAMESPACEURL,
                                           "param",
                                           null ,
                                           templateElements ,
                                           new XSLTAttributeDef[]{
                                                   nameAttrRequired,
                                                   selectAttrOpt }, 
                                           new ProcessorGlobalParamDecl(),
                                           ElemParam.class , 20, true),
                                  new XSLTElementDef(
                                           this,
                                           Constants.S_XSLNAMESPACEURL,
                                           "template",
                                           null ,
                                           templateElementsAndParams ,
                                           new XSLTAttributeDef[]{
                                                   matchAttrOpt,
                                                   nameAttrOpt_ERROR,
                                                   priorityAttr,
                                                   modeAttr,
                                                   spaceAttr }, 
                                           new ProcessorTemplate(), ElemTemplate.class , true, 20, true), 
                                  new XSLTElementDef(
                                           this,
                                           Constants.S_XSLNAMESPACEURL,
                                           "namespace-alias",
                                           null ,
                                          null ,  
                                           new XSLTAttributeDef[]{ 
                                                   stylesheetPrefixAttr,
                                                   resultPrefixAttr }, 
                                           new ProcessorNamespaceAlias(), null , 20, true),
                                  new XSLTElementDef(
                                           this,
                                           Constants.S_BUILTIN_EXTENSIONS_URL,
                                           "component",
                                           null ,
                                           new XSLTElementDef[]{
                                                    new XSLTElementDef(
                                                        this,
                                                        Constants.S_BUILTIN_EXTENSIONS_URL,
                                                        "script",
                                                        null ,
                                                    	new XSLTElementDef[]{ 
                                                        charData } ,
                                                        scriptAttrs, 
                                                        new ProcessorLRE(),
                                                        ElemExtensionScript.class , 20, true) },  
                                           componentAttrs, 
                                           new ProcessorLRE(), ElemExtensionDecl.class ),
                                  new XSLTElementDef(
                                           this,
                                           Constants.S_BUILTIN_OLD_EXTENSIONS_URL,
                                           "component",
                                           null ,
                                           new XSLTElementDef[]{
                                                    new XSLTElementDef(
                                                        this,
                                                        Constants.S_BUILTIN_OLD_EXTENSIONS_URL,
                                                        "script",
                                                        null ,
                                                    	new XSLTElementDef[]{ 
                                                        charData } ,
                                                        scriptAttrs, 
                                                        new ProcessorLRE(),
                                                        ElemExtensionScript.class , 20, true) },  
                                           componentAttrs, 
                                           new ProcessorLRE(), ElemExtensionDecl.class ),
                                  exsltFunction};  
    XSLTAttributeDef excludeResultPrefixesAttr =
      new XSLTAttributeDef(null, "exclude-result-prefixes",
                           XSLTAttributeDef.T_PREFIXLIST, false,false,XSLTAttributeDef.WARNING);
    XSLTAttributeDef extensionElementPrefixesAttr =
      new XSLTAttributeDef(null, "extension-element-prefixes",
                           XSLTAttributeDef.T_PREFIX_URLLIST, false,false,XSLTAttributeDef.WARNING);
    XSLTAttributeDef idAttr = new XSLTAttributeDef(null, "id",
                                XSLTAttributeDef.T_CDATA, false,false,XSLTAttributeDef.WARNING);
    XSLTAttributeDef versionAttrRequired = new XSLTAttributeDef(null,
                                             "version",
                                             XSLTAttributeDef.T_NMTOKEN,
                                             true,false,XSLTAttributeDef.WARNING);
    XSLTElementDef stylesheetElemDef = new XSLTElementDef(this,
                                         Constants.S_XSLNAMESPACEURL,
                                         "stylesheet", "transform",
                                         topLevelElements,
                                         new XSLTAttributeDef[]{
                                           extensionElementPrefixesAttr,
                                           excludeResultPrefixesAttr,
                                           idAttr,
                                           versionAttrRequired,
                                           spaceAttr }, new ProcessorStylesheetElement(),  
                                         null  ,
                                         true, -1, false);
    importDef.setElements(new XSLTElementDef[]{ stylesheetElemDef,
                                                resultElement,
                                                unknownElement });
    includeDef.setElements(new XSLTElementDef[]{ stylesheetElemDef,
                                                 resultElement,
                                                 unknownElement });
    build(null, null, null, new XSLTElementDef[]{ stylesheetElemDef,
                                                  whiteSpaceOnly,
                                                  resultElement,
                                                  unknownElement }, null,
                                                                    new ProcessorStylesheetDoc(),  
                                                                    null  
                                                                      );
  }
  private HashMap m_availElems = new HashMap();
  public HashMap getElemsAvailable() 
  {
    return m_availElems;
  }
  void addAvailableElement(QName elemName)
  {
    m_availElems.put(elemName, elemName);
  }
  public boolean elementAvailable(QName elemName)
  {
    return m_availElems.containsKey(elemName);
  }
}
