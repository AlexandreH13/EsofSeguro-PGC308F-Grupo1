package br.grupo1.transcricao.transcricao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TranscricaoRepository extends JpaRepository<Transcricao, UUID> {

    /**
     * RS01: a busca já recebe o promotor da sessão. Um id que não pertence a ele
     * devolve vazio, igual a um id inexistente, e o Backend nunca carrega o registro alheio.
     */
    Optional<Transcricao> findByIdAndPromotorId(UUID id, String promotorId);

    List<Transcricao> findByPromotorIdOrderBySolicitadaEmDesc(String promotorId);
}
