package com.financeiro.api.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.financeiro.api.dtos.CaixaDTO;
import com.financeiro.api.enteties.Caixa;
import com.financeiro.api.enums.AtivoInativoEnum;
import com.financeiro.api.exceptions.BusinessException;
import com.financeiro.api.repositories.CaixaRepository;
import com.financeiro.api.services.CaixaService;

@Service
public class CaixaServiceImpl implements CaixaService {

	private static final Logger log = LoggerFactory.getLogger(CaixaServiceImpl.class);

	@Autowired
	private CaixaRepository caixaRepository;
	
	@Override
	public CaixaDTO cadastrarCaixa(CaixaDTO dto) {
		log.debug("Usuario {}. Novo caixa cadastrado {}", dto.getIdUsuario(), dto.getNome());
		Caixa caixa = new Caixa(dto);
		caixa.setSituacao(AtivoInativoEnum.ATIVO);
		this.caixaRepository.save(caixa);
		return new CaixaDTO(caixa);
	}

	@Override
	public CaixaDTO alterarCaixa(CaixaDTO dto) throws BusinessException {
		log.debug("Usuario {}. Alteração caixa {}", dto.getIdUsuario(), dto.getNome());
		Caixa caixa = new Caixa(dto);
		if (caixa.getId() != null) {
			if (caixa.getSituacao().equals(AtivoInativoEnum.ATIVO)) {
				this.caixaRepository.save(caixa);
				return new CaixaDTO(caixa);
			} else throw new BusinessException("O caixa deve estar ativo para possibilitar atualização");
		}
		else throw new BusinessException("O caixa deve ser salvo antes de realizar uma alteração");
	}

	@Override
	public String habilitarCaixa(Long idCaixa, Long idUsuario) throws BusinessException {
		log.debug("Habilita caixa id {}", idCaixa);
		Caixa caixa  = findCaixaById(idCaixa, idUsuario);
		if(!caixa.getSituacao().equals(AtivoInativoEnum.ATIVO)) {
			caixa.setSituacao(AtivoInativoEnum.ATIVO);
			this.caixaRepository.save(caixa);
			return "Registro habilitado";
		} else throw new BusinessException("Caixa encontra-se ativo");
	}

	@Override
	public String desabilitarCaixa(Long idCaixa, Long idUsuario) throws BusinessException {
		log.debug("Desabilita caixa id {}", idCaixa);
		Caixa caixa  = findCaixaById(idCaixa, idUsuario);
		if(!caixa.getSituacao().equals(AtivoInativoEnum.INATIVO)) {
			caixa.setSituacao(AtivoInativoEnum.INATIVO);
			this.caixaRepository.save(caixa);
			return "Registro desabilitado";
		} else throw new BusinessException("Caixa encontra-se inativo");
	}

	private Caixa findCaixaById(Long idCaixa, Long idUsuario) throws BusinessException {
		return this.caixaRepository.findByIdAndIdUsuario(idCaixa, idUsuario).orElseThrow(() -> new BusinessException("Nenhum caixa retornado para o ID informado"));
	}

	@Override
	public List<CaixaDTO> findCaixaByIdUsuario(Long idUsuario) {
		return this.caixaRepository.findByIdUsuario(idUsuario).stream().map(CaixaDTO::new).collect(Collectors.toList());
	}

	@Override
	public List<CaixaDTO> findActiveCaixaByIdUsuario(Long idUsuario) {
		return this.caixaRepository.findByIdUsuarioAndSituacao(idUsuario, AtivoInativoEnum.ATIVO).stream().map(CaixaDTO::new).collect(Collectors.toList());
	}

}