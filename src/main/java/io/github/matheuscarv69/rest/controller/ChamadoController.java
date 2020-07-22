package io.github.matheuscarv69.rest.controller;

import io.github.matheuscarv69.domain.entity.Chamado;
import io.github.matheuscarv69.domain.enums.StatusChamado;
import io.github.matheuscarv69.exception.RegraNegocioException;
import io.github.matheuscarv69.rest.dto.AtualizacaoStatusChamadoDTO;
import io.github.matheuscarv69.rest.dto.ChamadoDTO;
import io.github.matheuscarv69.rest.dto.InformacoesChamadoDTO;
import io.github.matheuscarv69.service.ChamadoService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("api/chamados")
public class ChamadoController {

    private ChamadoService service;

    public ChamadoController(ChamadoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public Integer save(@RequestBody ChamadoDTO dto) {
        Chamado chamado = service.salvar(dto);
        return chamado.getId();
    }

    @GetMapping("{id}")
    public InformacoesChamadoDTO getById(@PathVariable("id") Integer id) {
        return service
                .buscarChamadoPorId(id)
                .map(chamado -> converter(chamado))
                .orElseThrow(() ->
                        new ResponseStatusException(NOT_FOUND, "Chamado não encontrado."));

    }

   @PatchMapping("{id}") // patchmapping só atualiza campos especificos do objeto, diferentemente do putmapping, onde todos os dados são atualizados
   @ResponseStatus(NO_CONTENT)
   public void updateStatus(@PathVariable("id") Integer id, @RequestBody AtualizacaoStatusChamadoDTO dto){

        String novoStatus = dto.getNovoStatus();

        service.atualizaStatus(id, StatusChamado.valueOf(novoStatus));

   }

    private InformacoesChamadoDTO converter(Chamado chamado) {

        String dataFinal = new String();
        String nomeTecn = new String();
        String matriculaTecn = new String();

        if (chamado.getDataFinal() == null) {
            dataFinal = "Chamado ainda não foi solucionado.";
        } else {
            dataFinal = chamado.getDataFinal().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }

        if(chamado.getTecnico() == null){
            nomeTecn = "Técnico não atribuído ao chamado.";
            matriculaTecn = "";
        }else{
            nomeTecn = chamado.getTecnico().getNome();
            matriculaTecn = chamado.getTecnico().getMatricula();
        }

        return InformacoesChamadoDTO
                .builder()
                .id(chamado.getId())
                .nome(chamado.getRequerente().getNome())
                .matricula(chamado.getRequerente().getMatricula())
                .titulo(chamado.getTitulo())
                .descricao(chamado.getDescricao())
                .tipo(chamado.getTipo().toString())
                .bloco(chamado.getBloco())
                .sala(chamado.getSala())
                .status(chamado.getStatus().name())
                .dataInicio(chamado.getDataInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyy")))
                .dataFinal(dataFinal)
                .nomeTecn(nomeTecn)
                .matriculaTecn(matriculaTecn)
                .build();
    }

}
