public class test {
    public void _jspService(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException, ServletException {
        PageContext pageContext = null;
        HttpSession session = null;
        ServletContext application = null;
        ServletConfig config = null;
        JspWriter out = null;
        Object page = this;
        JspWriter _jspx_out = null;
        PageContext _jspx_page_context = null;
        try {
            response.setContentType("text/html; charset=ISO-8859-1");
            pageContext = _jspxFactory.getPageContext(this, request, response, null, true, 8192, true);
            _jspx_page_context = pageContext;
            application = pageContext.getServletContext();
            config = pageContext.getServletConfig();
            session = pageContext.getSession();
            out = pageContext.getOut();
            _jspx_out = out;
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("<script src=\"./js/jQuery.js\" type=\"text/javascript\"></script>\r\n");
            out.write("<script src=\"./js/calendar.js\" type=\"text/javascript\"></script>\r\n");
            out.write("<script src=\"./js/jquery.alphanumeric.pack.js\" type=\"text/javascript\"></script>\r\n");
            out.write("<script src=\"./js/jquery.tablesorter.js\" type=\"text/javascript\"></script>\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("<script type=\"text/javascript\">\r\n");
            out.write("\r\n");
            out.write("function onSuccess(transport, element) {\r\n");
            out.write("    element.innerHTML = this.editField.value;\r\n");
            out.write("\tnew Effect.Highlight(element, {startcolor: this.options.highlightcolor});\r\n");
            out.write("}\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("function pesquisar(){\r\n");
            out.write("  \t  \tform.metodo.value = 'pesquisar';\r\n");
            out.write("  \t  \tform.submit();\r\n");
            out.write("}\r\n");
            out.write("\r\n");
            out.write("function carregarMascaras() {\r\n");
            out.write("\r\n");
            out.write("\t\tvalorMask = new Mask(\"#.00\", \"number\");\r\n");
            out.write("\t\tvalorMask.attach(document.formRecibo.valorPago);\r\n");
            out.write("\t\t\r\n");
            out.write("\t\t
            out.write("\t\t
            out.write("}\r\n");
            out.write("\r\n");
            out.write("function submeter(){\t\r\n");
            out.write("\tif(document.getElementById('vpago').disabled){\r\n");
            out.write("\t\talert('Pagamento já informado.');\r\n");
            out.write("\t\tdocument.getElementById('valorPagamento').disabled = 1;\r\n");
            out.write("\t\t\tdocument.getElementById('valorPago').style.display = 'none';\r\n");
            out.write("\t\t\tdocument.getElementById('vpago').style.display = 'block';\r\n");
            out.write("\t\t\tdocument.getElementById('vpago').disabled = 1;\r\n");
            out.write("\t\t\tdocument.getElementById('submete').style.display = 'none';\r\n");
            out.write("\t\t\tdocument.getElementById('zera').style.display = 'block';\r\n");
            out.write("\t}else{\t\t\t\r\n");
            out.write("\t\tvar vpagamento = parseFloat(document.getElementById('totalDivida').value);\r\n");
            out.write("\t\tvar vpago = parseFloat(document.getElementById('valorPago').value);\r\n");
            out.write("\t\t\t\r\n");
            out.write("\t\tif( vpago <= vpagamento && vpago > 0){\r\n");
            out.write("\t\t\tdocument.getElementById('valorPagamento').disabled = 1;\r\n");
            out.write("\t\t\tdocument.getElementById('valorPago').style.display = 'none';\r\n");
            out.write("\t\t\tdocument.getElementById('vpago').style.display = 'block';\r\n");
            out.write("\t\t\tdocument.getElementById('valorPagamento').value = vpagamento-vpago;\r\n");
            out.write("\t\t\tdocument.getElementById('vpago').value = document.getElementById('valorPago').value;\r\n");
            out.write("\t\t\tdocument.getElementById('vpago').disabled = 1;\r\n");
            out.write("\t\t\tdocument.getElementById('zera').style.display = 'block';\r\n");
            out.write("\t\t\tdocument.getElementById('submete').style.display = 'none';\r\n");
            out.write("\t\t\t\r\n");
            out.write("\t\t\tdocument.getElementById('metodo').value = 'gerar';\r\n");
            out.write("\t\t\t\r\n");
            out.write("\t\t}\r\n");
            out.write("\t\tdocument.forms[0].submit();\r\n");
            out.write("\t}\r\n");
            out.write("\t\t\r\n");
            out.write("}\r\n");
            out.write("\r\n");
            out.write("\tfunction mudarPagamento(){\r\n");
            out.write("\t\t\r\n");
            out.write("\t\tfor(i =0; i <= 1; i++){\r\n");
            out.write("\t\t\tif(document.forms[0].radioTipoServico[i].checked){\r\n");
            out.write("\t\t\t\tdocument.getElementById('formaPagamento').value = i;\r\n");
            out.write("\t\t\t\t
            out.write("\t\t\t}\r\n");
            out.write("\t\t} \r\n");
            out.write("\t\t\r\n");
            out.write("\t\tvar obj = document.getElementById('formaPagamento').value;\r\n");
            out.write("\t\tif(obj == 1){\r\n");
            out.write("\t\t
            out.write("\t\t\tdocument.getElementById('numCheque').style.display = 'block';\r\n");
            out.write("\t\t}else{\r\n");
            out.write("\t\t
            out.write("\t\t\tdocument.getElementById('numCheque').style.display = 'none';\r\n");
            out.write("\t\t}\r\n");
            out.write("\t}\r\n");
            out.write("\t\r\n");
            out.write("\tfunction copiarPaciente(){\r\n");
            out.write("\t\tdocument.getElementById('nomeEmitente').value = document.getElementById('nomePaciente').value;\r\n");
            out.write("\t}\r\n");
            out.write("\t\r\n");
            out.write("\tfunction inicializar(){\r\n");
            out.write("\t\tvar a = document.getElementById('formaPagamento').value;\r\n");
            out.write("\t\tif(document.getElementById('radioTipoServico') != null){\r\n");
            out.write("\t\t\tdocument.forms[0].radioTipoServico[a].checked = 1;\r\n");
            out.write("\t\t}\r\n");
            out.write("\t}\r\n");
            out.write("</script>\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            org.apache.jasper.runtime.JspRuntimeLibrary.include(request, response, "/header.jsp", out, true);
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("<div class=\"breadcrumb\">\r\n");
            out.write("\t");
            if (_jspx_meth_html_005flink_005f0(_jspx_page_context)) return;
            out.write("\r\n");
            out.write("\t\t&raquo;  ");
            if (_jspx_meth_html_005flink_005f1(_jspx_page_context)) return;
            out.write("\r\n");
            out.write("\t\t&raquo;<a class=\"ativo\" href=\"#\">Geração de Recibo</a> </div>\r\n");
            out.write("<div id=\"conteudo_cont\">\r\n");
            if (_jspx_meth_html_005fform_005f0(_jspx_page_context)) return;
            out.write("\r\n");
            out.write("</div>\r\n");
        } catch (Throwable t) {
            if (!(t instanceof SkipPageException)) {
                out = _jspx_out;
                if (out != null && out.getBufferSize() != 0) try {
                    out.clearBuffer();
                } catch (java.io.IOException e) {
                }
                if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
            }
        } finally {
            _jspxFactory.releasePageContext(_jspx_page_context);
        }
    }
}