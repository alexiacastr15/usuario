package com.alexiadev.usuario.business;

import com.alexiadev.usuario.business.converter.UsuarioConverter;
import com.alexiadev.usuario.business.dto.UsuarioDTO;
import com.alexiadev.usuario.infrastructure.entity.Usuario;
import com.alexiadev.usuario.infrastructure.exceptions.ConflictException;
import com.alexiadev.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.alexiadev.usuario.infrastructure.repository.UsuarioRepository;
import com.alexiadev.usuario.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class UsuarioService {
    private final UsuarioRepository usuarioRepository; //injeção de dependência da interface UsuarioRepository
    private final UsuarioConverter usuarioConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;



    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO){ //recebeu objeto usuarioDTO
        emailExiste(usuarioDTO.getEmail());
        usuarioDTO.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO); //transformou em um usuario (entity)
        usuario = usuarioRepository.save(usuario); //salvou a info no banco de dados, que retorna um usuario (entity)
        return usuarioConverter.paraUsuarioDTO(usuario); //converteu novamente para usuarioDTO

        /*return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario)); -> mesma coisa*/
    }

    public void emailExiste(String email){
        try{
            boolean existe = verificarEmailExistente(email);
            if(existe){
                throw new ConflictException("Email já cadastrado" + email);
            }
        } catch (ConflictException e){
            throw new ConflictException("Email já cadastrado" + e.getCause());
        }
    }

    public boolean verificarEmailExistente(String email){
        return usuarioRepository.existsByEmail(email);
    }

    public Usuario buscarUsuarioPorEmail(String email){
        return usuarioRepository.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("Email não encontrado" + email));
    }

    public void deletaUsuarioPorEmail(String email){
        usuarioRepository.deleteByEmail(email);
    }

    public UsuarioDTO atualizaDadosUsuario(String token, UsuarioDTO dto){
        //Buscar email atraves do token (tirar obrigatoriedade do email)
        String email = jwtUtil.extrairEmailToken(token.substring(7));

        //Criptografia de senha
        dto.setSenha(dto.getSenha() != null ? passwordEncoder.encode(dto.getSenha()) : null);

        //Buscar dados do usuario no banco de dados
        Usuario usuarioEntity = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email não localizado")); //obrigatorio por estar usando um opcional

        //Mesclou dados que recebeu na requisição DTO com os dados do banco de dados
        Usuario usuario = usuarioConverter.updateUsuario(dto, usuarioEntity);

        //salvou dados do usuário convertido e depois pegou o retorno e converteu para UsuárioDTO
        return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario));
    }
}
