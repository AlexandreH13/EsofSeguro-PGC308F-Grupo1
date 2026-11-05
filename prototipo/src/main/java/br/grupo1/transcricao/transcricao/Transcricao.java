package br.grupo1.transcricao.transcricao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro de uma transcrição (ver "Registro de transcrições" no README dos casos de uso).
 * O texto transcrito fica numa coluna para simular o Storage neste protótipo.
 */
@Entity
public class Transcricao {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String promotorId;

    @Column(nullable = false)
    private String processo;

    private String vara;

    @Column(nullable = false)
    private String midia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTranscricao status;

    private String detalheStatus;

    @Column(nullable = false)
    private Instant solicitadaEm;

    @Column(length = 200_000)
    private String texto;

    protected Transcricao() {
        // exigido pelo JPA
    }

    public Transcricao(String promotorId, String processo, String vara, String midia,
                       StatusTranscricao status, String detalheStatus, Instant solicitadaEm, String texto) {
        // RS01: id gerado pela aplicação como UUID versão 4, nunca sequencial (CWE-340)
        this.id = UUID.randomUUID();
        this.promotorId = promotorId;
        this.processo = processo;
        this.vara = vara;
        this.midia = midia;
        this.status = status;
        this.detalheStatus = detalheStatus;
        this.solicitadaEm = solicitadaEm;
        this.texto = texto;
    }

    public UUID getId() { return id; }
    public String getPromotorId() { return promotorId; }
    public String getProcesso() { return processo; }
    public String getVara() { return vara; }
    public String getMidia() { return midia; }
    public StatusTranscricao getStatus() { return status; }
    public String getDetalheStatus() { return detalheStatus; }
    public Instant getSolicitadaEm() { return solicitadaEm; }
    public String getTexto() { return texto; }
}
