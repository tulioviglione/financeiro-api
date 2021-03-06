package com.financeiro.api.services;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.financeiro.api.dtos.CaixaDTO;
import com.financeiro.api.enteties.Caixa;
import com.financeiro.api.enteties.Usuario;
import com.financeiro.api.enums.AtivoInativoEnum;
import com.financeiro.api.enums.PerfilEnum;
import com.financeiro.api.enums.SituacaoUsuarioEnum;
import com.financeiro.api.enums.TipoCaixaEnum;
import com.financeiro.api.exceptions.BusinessException;
import com.financeiro.api.repositories.CaixaRepository;
import com.financeiro.api.repositories.UsuarioRepository;
import com.financeiro.api.util.ConstantesUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
class CaixaServiceTest {

	@MockBean
	private CaixaRepository caixaRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private CaixaService caixaService;

	private Usuario usuario;
	private Caixa caixaAtivo;
	private Caixa caixaInativo;

	@BeforeEach
	public void setUp() throws Exception {
		this.usuario = new Usuario();
		this.usuario.setEmail(ConstantesUtil.Usuario.EMAIL_VALIDO);
		this.usuario.setLogin(ConstantesUtil.Usuario.LOGIN);
		this.usuario.setSenha(ConstantesUtil.Usuario.SENHA_VALIDA);
		this.usuario.setPerfil(PerfilEnum.ADMIN);
		this.usuario.setSituacao(SituacaoUsuarioEnum.ATIVO);
		this.usuario = this.usuarioRepository.save(this.usuario);

		this.caixaAtivo = new Caixa();
		this.caixaAtivo.setUsuario(this.usuario);
		this.caixaAtivo.setNome("NomeAtivo");
		this.caixaAtivo.setDescricao("DescricaoAtivo");
		this.caixaAtivo.setTipoCaixa(TipoCaixaEnum.BANCO);
		this.caixaAtivo.setSituacao(AtivoInativoEnum.ATIVO);

		this.caixaInativo = new Caixa(1L);
		this.caixaInativo.setUsuario(this.usuario);
		this.caixaInativo.setNome("nomeInativo");
		this.caixaInativo.setDescricao("descricaoInativo");
		this.caixaInativo.setTipoCaixa(TipoCaixaEnum.BANCO);
		this.caixaInativo.setSituacao(AtivoInativoEnum.INATIVO);

		BDDMockito.given(this.caixaRepository.findByIdUsuario(Mockito.anyLong())).willReturn(new ArrayList<>());
		BDDMockito.given(this.caixaRepository.findByIdUsuarioAndSituacao(Mockito.anyLong(), Mockito.any()))
				.willReturn(new ArrayList<>());
	}

	@AfterEach
	public final void tearDown() {
		this.usuarioRepository.deleteAll();
	}

	@Test
	void cadastrarCaixaTest() {
		Caixa caixa = new Caixa();
		caixa.setNome("testeNome");
		caixa.setDescricao("testeDescricao");
		caixa.setTipoCaixa(TipoCaixaEnum.BANCO);
		caixa.setUsuario(this.usuario);
		Assertions.assertEquals(AtivoInativoEnum.ATIVO,
				this.caixaService.cadastrarCaixa(new CaixaDTO(caixa)).getSituacao());
	}

	@Test
	void atualizarCaixaTeste() throws BusinessException {
		Assertions.assertThrows(BusinessException.class, () -> {
			this.caixaService.alterarCaixa(new CaixaDTO(this.caixaAtivo));
		});
		Assertions.assertThrows(BusinessException.class, () -> {
			this.caixaService.alterarCaixa(new CaixaDTO(this.caixaInativo));
		});
		CaixaDTO dto = this.caixaService.cadastrarCaixa(new CaixaDTO(this.caixaAtivo));
		dto.setId(1L);
		Assertions.assertNotNull(this.caixaService.alterarCaixa(dto));
	}

	@Test
	void habilitarCaixaTest() throws BusinessException {
		Assertions.assertThrows(BusinessException.class, () -> {
			this.caixaService.habilitarCaixa(1L, this.usuario.getId());
		});

		BDDMockito.given(this.caixaRepository.findByIdAndIdUsuario(Mockito.anyLong(), Mockito.anyLong()))
				.willReturn(Optional.of(this.caixaAtivo));
		Assertions.assertThrows(BusinessException.class, () -> {
			this.caixaService.habilitarCaixa(1L, this.usuario.getId());
		});

		BDDMockito.given(this.caixaRepository.findByIdAndIdUsuario(Mockito.anyLong(), Mockito.anyLong()))
				.willReturn(Optional.of(this.caixaInativo));
		Assertions.assertNotNull(this.caixaService.habilitarCaixa(1L, this.usuario.getId()));
	}

	@Test
	void DesabilitarCaixaTest() throws BusinessException {
		Assertions.assertThrows(BusinessException.class, () -> {
			this.caixaService.desabilitarCaixa(1L, this.usuario.getId());
		});

		BDDMockito.given(this.caixaRepository.findByIdAndIdUsuario(Mockito.anyLong(), Mockito.anyLong()))
				.willReturn(Optional.of(this.caixaInativo));
		Assertions.assertThrows(BusinessException.class, () -> {
			this.caixaService.desabilitarCaixa(1L, this.usuario.getId());
		});

		BDDMockito.given(this.caixaRepository.findByIdAndIdUsuario(Mockito.anyLong(), Mockito.anyLong()))
				.willReturn(Optional.of(this.caixaAtivo));
		Assertions.assertNotNull(this.caixaService.desabilitarCaixa(1L, this.usuario.getId()));
	}

	@Test
	void findActiveCaixaByIdUsuarioTest() {
		Assertions.assertNotNull(this.caixaService.findActiveCaixaByIdUsuario(1L));
	}

	@Test
	void findCaixaByIdUsuarioTest() {
		Assertions.assertNotNull(this.caixaService.findCaixaByIdUsuario(1L));
	}

}