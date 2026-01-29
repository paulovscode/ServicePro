package com.servicepro.backend.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.servicepro.backend.model.Servico;
import com.servicepro.backend.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfService {
    
    // Fontes
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);
    private static final Font BOLD_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.BLACK);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC, BaseColor.GRAY);
    
    // Cores
    private static final BaseColor PRIMARY_COLOR = new BaseColor(41, 128, 185); // Azul
    private static final BaseColor SECONDARY_COLOR = new BaseColor(52, 152, 219); // Azul claro
    private static final BaseColor SUCCESS_COLOR = new BaseColor(46, 204, 113); // Verde
    private static final BaseColor LIGHT_GRAY = new BaseColor(245, 245, 245);
    
    /**
     * Gera comprovante profissional em PDF
     */
    public byte[] gerarComprovanteProfissional(Servico servico) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            
            // Adicionar evento para header/footer
            writer.setPageEvent(new PdfPageEvent());
            
            document.open();
            
            // ========== CABEÇALHO ==========
            adicionarCabecalho(document);
            
            // ========== TÍTULO ==========
            adicionarTitulo(document, "COMPROVANTE DE SERVIÇO");
            
            // ========== INFORMAÇÕES DO SERVIÇO ==========
            adicionarSecao(document, "INFORMAÇÕES DO SERVIÇO");
            adicionarTabelaServico(document, servico);
            
            // ========== DADOS DO CLIENTE ==========
            adicionarSecao(document, "DADOS DO CLIENTE");
            adicionarTabelaCliente(document, servico);
            
            // ========== DADOS DO PRESTADOR ==========
            adicionarSecao(document, "DADOS DO PRESTADOR");
            adicionarTabelaPrestador(document, servico);
            
            // ========== OBSERVAÇÕES ==========
            if (servico.getObservacoes() != null && !servico.getObservacoes().isEmpty()) {
                adicionarSecao(document, "OBSERVAÇÕES");
                adicionarObservacoes(document, servico.getObservacoes());
            }
            
            // ========== RODAPÉ ==========
            adicionarRodape(document);
            
            // ========== QR CODE (opcional) ==========
            adicionarQrCode(document, servico);
            
            document.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gera PDF simples (para compatibilidade)
     */
    public byte[] gerarComprovante(Servico servico) {
        return gerarComprovanteProfissional(servico);
    }
    
    /**
     * Adiciona cabeçalho com logo
     */
    private void adicionarCabecalho(Document document) throws DocumentException, IOException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20);
        
        // Logo (esquerda)
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        
        // Texto do logo
        Paragraph logo = new Paragraph("ServicePro", TITLE_FONT);
        logo.setAlignment(Element.ALIGN_LEFT);
        logoCell.addElement(logo);
        
        Paragraph slogan = new Paragraph("Sistema de Agendamento Profissional", SMALL_FONT);
        slogan.setAlignment(Element.ALIGN_LEFT);
        logoCell.addElement(slogan);
        
        // Data (direita)
        PdfPCell dataCell = new PdfPCell();
        dataCell.setBorder(Rectangle.NO_BORDER);
        dataCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        String dataEmissao = DateUtils.formatDateTime(java.time.LocalDateTime.now());
        Paragraph data = new Paragraph("Emitido em: " + dataEmissao, SMALL_FONT);
        data.setAlignment(Element.ALIGN_RIGHT);
        dataCell.addElement(data);
        
        table.addCell(logoCell);
        table.addCell(dataCell);
        
        document.add(table);
        
        // Linha divisória
        adicionarLinhaDivisoria(document);
    }
    
    /**
     * Adiciona título
     */
    private void adicionarTitulo(Document document, String titulo) throws DocumentException {
        Paragraph tituloParagraph = new Paragraph(titulo, TITLE_FONT);
        tituloParagraph.setAlignment(Element.ALIGN_CENTER);
        tituloParagraph.setSpacingBefore(30);
        tituloParagraph.setSpacingAfter(20);
        document.add(tituloParagraph);
    }
    
    /**
     * Adiciona seção
     */
    private void adicionarSecao(Document document, String tituloSecao) throws DocumentException {
        Paragraph secao = new Paragraph(tituloSecao, SUBTITLE_FONT);
        secao.setSpacingBefore(20);
        secao.setSpacingAfter(10);
        document.add(secao);
    }
    
    /**
     * Adiciona tabela de serviço
     */
    private void adicionarTabelaServico(Document document, Servico servico) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);
        
        // Estilo das células
        PdfPCell cellStyle = new PdfPCell();
        cellStyle.setPadding(8);
        cellStyle.setBorderColor(BaseColor.LIGHT_GRAY);
        
        adicionarLinhaTabela(table, "Nº do Serviço:", servico.getId().toString(), cellStyle);
        adicionarLinhaTabela(table, "Status:", getStatusFormatado(servico.getStatus().name()), cellStyle);
        
        String dataFormatada = servico.getDataAgendamento()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));
        adicionarLinhaTabela(table, "Data Agendada:", dataFormatada, cellStyle);
        
        if (servico.getDataConclusao() != null) {
            String conclusaoFormatada = servico.getDataConclusao()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));
            adicionarLinhaTabela(table, "Data Conclusão:", conclusaoFormatada, cellStyle);
        }
        
        String valorFormatado = String.format("R$ %.2f", servico.getValor());
        adicionarLinhaTabela(table, "Valor do Serviço:", valorFormatado, cellStyle);
        
        document.add(table);
    }
    
    /**
     * Adiciona tabela de cliente
     */
    private void adicionarTabelaCliente(Document document, Servico servico) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);
        
        PdfPCell cellStyle = new PdfPCell();
        cellStyle.setPadding(8);
        cellStyle.setBorderColor(BaseColor.LIGHT_GRAY);
        
        adicionarLinhaTabela(table, "Nome:", servico.getClienteNome(), cellStyle);
        
        if (servico.getClienteTelefone() != null && !servico.getClienteTelefone().isEmpty()) {
            adicionarLinhaTabela(table, "Telefone:", servico.getClienteTelefone(), cellStyle);
        }
        
        if (servico.getClienteEmail() != null && !servico.getClienteEmail().isEmpty()) {
            adicionarLinhaTabela(table, "E-mail:", servico.getClienteEmail(), cellStyle);
        }
        
        adicionarLinhaTabela(table, "Endereço:", servico.getEndereco(), cellStyle);
        
        document.add(table);
    }
    
    /**
     * Adiciona tabela de prestador
     */
    private void adicionarTabelaPrestador(Document document, Servico servico) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);
        
        PdfPCell cellStyle = new PdfPCell();
        cellStyle.setPadding(8);
        cellStyle.setBorderColor(BaseColor.LIGHT_GRAY);
        
        adicionarLinhaTabela(table, "Nome:", servico.getPrestador().getNomeCompleto(), cellStyle);
        adicionarLinhaTabela(table, "Telefone:", servico.getPrestador().getTelefone(), cellStyle);
        adicionarLinhaTabela(table, "E-mail:", servico.getPrestador().getEmail(), cellStyle);
        
        document.add(table);
    }
    
    /**
     * Adiciona observações
     */
    private void adicionarObservacoes(Document document, String observacoes) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(20);
        
        PdfPCell cell = new PdfPCell(new Paragraph(observacoes, NORMAL_FONT));
        cell.setPadding(10);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        cell.setBackgroundColor(LIGHT_GRAY);
        
        table.addCell(cell);
        document.add(table);
    }
    
    /**
     * Adiciona linha divisória
     */
    private void adicionarLinhaDivisoria(Document document) throws DocumentException {
        Paragraph linha = new Paragraph();
        linha.add(new Chunk("\n"));
        document.add(linha);
        
        PdfPTable linhaTable = new PdfPTable(1);
        linhaTable.setWidthPercentage(100);
        linhaTable.setSpacingBefore(5);
        linhaTable.setSpacingAfter(5);
        
        PdfPCell linhaCell = new PdfPCell();
        linhaCell.setBorder(Rectangle.NO_BORDER);
        linhaCell.setFixedHeight(1);
        linhaCell.setBackgroundColor(SECONDARY_COLOR);
        
        linhaTable.addCell(linhaCell);
        document.add(linhaTable);
    }
    
    /**
     * Adiciona rodapé
     */
    private void adicionarRodape(Document document) throws DocumentException {
        Paragraph rodape = new Paragraph();
        rodape.add(new Chunk("\n\n"));
        
        PdfPTable rodapeTable = new PdfPTable(1);
        rodapeTable.setWidthPercentage(100);
        rodapeTable.setSpacingBefore(30);
        
        PdfPCell rodapeCell = new PdfPCell();
        rodapeCell.setBorder(Rectangle.NO_BORDER);
        rodapeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        Paragraph textoRodape = new Paragraph(
            "Este documento foi gerado automaticamente pelo sistema ServicePro.\n" +
            "Para mais informações, entre em contato: contato@servicepro.com.br",
            SMALL_FONT
        );
        textoRodape.setAlignment(Element.ALIGN_CENTER);
        
        rodapeCell.addElement(textoRodape);
        rodapeTable.addCell(rodapeCell);
        
        document.add(rodapeTable);
    }
    
    /**
     * Adiciona QR Code (opcional)
     */
    private void adicionarQrCode(Document document, Servico servico) throws DocumentException {
        // Implementação de QR Code seria adicionada aqui
        // Pode conter link para validação online do comprovante
    }
    
    /**
     * Adiciona linha na tabela
     */
    private void adicionarLinhaTabela(PdfPTable table, String label, String valor, PdfPCell style) {
        PdfPCell labelCell = new PdfPCell(new Paragraph(label, BOLD_FONT));
        labelCell.setPadding(8);
        labelCell.setBorderColor(BaseColor.LIGHT_GRAY);
        labelCell.setBackgroundColor(LIGHT_GRAY);
        
        PdfPCell valorCell = new PdfPCell(new Paragraph(valor != null ? valor : "N/A", NORMAL_FONT));
        valorCell.setPadding(8);
        valorCell.setBorderColor(BaseColor.LIGHT_GRAY);
        
        table.addCell(labelCell);
        table.addCell(valorCell);
    }
    
    /**
     * Formata status para português
     */
    private String getStatusFormatado(String status) {
        switch (status.toUpperCase()) {
            case "AGENDADO": return "Agendado";
            case "CONFIRMADO": return "Confirmado";
            case "EM_ANDAMENTO": return "Em Andamento";
            case "CONCLUIDO": return "Concluído";
            case "CANCELADO": return "Cancelado";
            default: return status;
        }
    }
    
    /**
     * Classe interna para eventos de página (header/footer)
     */
    class PdfPageEvent extends PdfPageEventHelper {
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                // Adicionar número da página
                PdfPTable footer = new PdfPTable(1);
                footer.setTotalWidth(document.getPageSize().getWidth() - 80);
                footer.setLockedWidth(true);
                
                PdfPCell cell = new PdfPCell(new Paragraph(
                    "Página " + writer.getPageNumber(),
                    new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.GRAY)
                ));
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                
                footer.addCell(cell);
                footer.writeSelectedRows(0, -1, 40, 30, writer.getDirectContent());
                
            } catch (Exception e) {
                // Ignorar erros no footer
            }
        }
    }
}