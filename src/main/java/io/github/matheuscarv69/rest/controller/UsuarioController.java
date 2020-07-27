package io.github.matheuscarv69.rest.controller;

import io.github.matheuscarv69.domain.entity.Chamado;
import io.github.matheuscarv69.domain.entity.Usuario;
import io.github.matheuscarv69.domain.repository.UsuarioRepository;
import io.github.matheuscarv69.exception.UsuarioNaoEncontradoException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private UsuarioRepository repository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.repository = usuarioRepository;
    }

    @PostMapping // salva um user
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario save(@RequestBody Usuario usuario) {
        return repository.save(usuario);
    }

    @PutMapping("{id}") // atualiza um user
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable Integer id, @RequestBody Usuario usuario) {

        repository
                .findById(id)
                .map(usuarioExistente -> {
                    usuario.setId(usuarioExistente.getId());
                    repository.save(usuario);
                    return usuarioExistente;
                }).orElseThrow(() ->
                new UsuarioNaoEncontradoException());

    }

    @DeleteMapping("{id}") // deleta um user
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        repository
                .findById(id)
                .map(usuarioExistente -> {
                    repository.delete(usuarioExistente);
                    return usuarioExistente;
                }).orElseThrow(() ->
                new UsuarioNaoEncontradoException());
    }

    @GetMapping("{id}") // busca um user por id
    public Usuario getUsuarioById(@PathVariable Integer id) {

        return repository
                .findById(id)
                .orElseThrow(() ->
                        new UsuarioNaoEncontradoException());
    }

    @GetMapping("/tecn")
    public List<Usuario> findTecnicos() {
        List<Usuario> list = repository.findTecnicos();

        if (list.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum técnico cadastrado.");
        }

        return list;
    }

    @GetMapping("/admin")
    public List<Usuario> findAdministradores() {
        List<Usuario> list = repository.findAdministradores();

        if (list.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum administrador cadastrado.");
        }

        return list;
    }

    @PutMapping("/admin/setTecn/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setTecn(@PathVariable Integer id, @RequestBody Usuario tecn) {

        repository.findById(id)
                .map(usuario -> {
                    if (usuario.isTecn() && tecn.isTecn()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já é técnico.");
                    }
                    usuario.setTecn(tecn.isTecn());
                    repository.save(usuario);

                    return usuario;
                }).orElseThrow(() ->
                new UsuarioNaoEncontradoException());
    }

    @PutMapping("/admin/setAdmin/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setAdmin(@PathVariable Integer id, @RequestBody Usuario admin) {

        repository.findById(id)
                .map(usuario -> {
                    if (usuario.isAdmin() && admin.isAdmin()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já é administrador.");
                    }
                    usuario.setAdmin(admin.isAdmin());
                    repository.save(usuario);

                    return usuario;
                }).orElseThrow(() ->
                new UsuarioNaoEncontradoException());
    }

    @GetMapping("/chamadosReq/{id}") // busca os chamados que um user fez
    public Set<Chamado> findChamadosReq(@PathVariable Integer id) {
        Usuario user = repository.findById(id)
                .orElseThrow(() ->
                        new UsuarioNaoEncontradoException());

        user = repository.findUsuarioFetchChamadosReq(user.getId());

        return user.getChamadosReq();
    }

    @GetMapping("/chamadosTecn/{id}") // busca os chamados que um tecnico está atribuido
    public Set<Chamado> findChamadosTecn(@PathVariable Integer id) {

        Usuario user = repository.findById(id)
                .orElseThrow(() ->
                        new UsuarioNaoEncontradoException());

        if (user.isTecn()) {
            user = repository.findUsuarioFetchChamadosTecn(user.getId());
            return user.getChamadosTecn();
        } else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário não é técnico.");

    }

    @GetMapping // busca por parametros: No campo Query defina qual propriedade quer buscar
    public List<Usuario> find(Usuario filtro) {

        ExampleMatcher matcher = ExampleMatcher
                .matching()
                .withIgnoreCase()
                .withStringMatcher(
                        ExampleMatcher.StringMatcher.CONTAINING);

        Example example = Example.of(filtro, matcher);

        return repository.findAll(example);
    }

}
