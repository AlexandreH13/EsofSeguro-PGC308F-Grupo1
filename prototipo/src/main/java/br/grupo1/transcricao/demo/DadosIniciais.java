package br.grupo1.transcricao.demo;

import br.grupo1.transcricao.transcricao.StatusTranscricao;
import br.grupo1.transcricao.transcricao.Transcricao;
import br.grupo1.transcricao.transcricao.TranscricaoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** Transcrições de exemplo, as mesmas das telas T3 e T4, para dois promotores. */
@Component
public class DadosIniciais implements CommandLineRunner {

    // Ids dos promotores como constam no provedor de identidade
    public static final String PROMOTOR_A = "promotor-a";
    public static final String PROMOTOR_B = "promotor-b";

    private static final String PROCESSO_1 = "0001234-56.2025.8.26.0100";
    private static final String PROCESSO_2 = "0007890-12.2024.8.26.0050";
    private static final String PROCESSO_3 = "0004567-89.2025.8.26.0100";

    private static final String TEXTO_DEPOIMENTO = """
            00:00:04 Promotor: Declare seu nome completo e a relação que tem com os fatos narrados na denúncia.
            00:00:11 Testemunha: Sou vizinha do imóvel onde ocorreu o fato. Moro no mesmo prédio há cerca de seis anos.
            00:00:29 Promotor: Na noite de 14 de março, a senhora estava em casa?
            00:00:34 Testemunha: Estava. Cheguei do trabalho por volta das vinte e uma horas e não saí mais.
            00:00:48 Promotor: O que a senhora ouviu ou viu a partir desse horário?
            00:00:55 Testemunha: Por volta das vinte e duas horas ouvi uma discussão no apartamento ao lado. Vozes alteradas, depois um barulho de objeto caindo. Durou uns dez minutos.
            00:01:20 Promotor: A senhora conseguiu identificar quantas pessoas participavam da discussão?
            00:01:26 Testemunha: Pelo menos duas vozes, uma masculina e uma feminina. Não reconheci a voz masculina.
            """;

    private static final String TEXTO_VITIMA = """
            00:00:03 Promotora: A senhora pode relatar o que aconteceu na tarde de 2 de maio?
            00:00:09 Vítima: Eu estava saindo da agência quando um homem de moto encostou e pediu a bolsa.
            00:00:21 Promotora: Ele estava armado?
            00:00:24 Vítima: Mostrou o cabo de uma arma na cintura. Entreguei a bolsa e ele saiu em alta velocidade.
            """;

    private final TranscricaoRepository repositorio;

    public DadosIniciais(TranscricaoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void run(String... args) {
        if (repositorio.count() > 0) {
            return;
        }
        repositorio.save(new Transcricao(PROMOTOR_A, PROCESSO_1, "2ª Vara Criminal da Capital",
                "Depoimento_testemunha_01.mp3", StatusTranscricao.FINALIZADA, null,
                em(10, 9, 14, 32), TEXTO_DEPOIMENTO));
        repositorio.save(new Transcricao(PROMOTOR_A, PROCESSO_1, "2ª Vara Criminal da Capital",
                "Inquerito_depoimento_02.mp3", StatusTranscricao.EM_PROCESSAMENTO, "iniciado há 12 min",
                em(12, 9, 9, 15), null));
        repositorio.save(new Transcricao(PROMOTOR_A, PROCESSO_1, "2ª Vara Criminal da Capital",
                "Oitiva_investigado.mp4", StatusTranscricao.NA_FILA, "fila de mídias grandes",
                em(12, 9, 10, 2), null));
        repositorio.save(new Transcricao(PROMOTOR_A, PROCESSO_2, "1ª Vara do Júri",
                "Audiencia_preliminar.mp3", StatusTranscricao.FALHA, "tempo limite de 1 hora excedido",
                em(5, 9, 16, 40), null));
        repositorio.save(new Transcricao(PROMOTOR_B, PROCESSO_3, "3ª Vara Criminal da Capital",
                "Depoimento_vitima.mp3", StatusTranscricao.FINALIZADA, null,
                em(8, 9, 11, 5), TEXTO_VITIMA));
    }

    private static Instant em(int dia, int mes, int hora, int minuto) {
        return ZonedDateTime.of(2026, mes, dia, hora, minuto, 0, 0, ZoneId.of("America/Sao_Paulo")).toInstant();
    }
}
