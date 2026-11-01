package br.grupo1.transcricao.transcricao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TranscricaoRepository extends JpaRepository<Transcricao, UUID> {
}
