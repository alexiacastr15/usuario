package com.alexiadev.usuario.business;

import com.alexiadev.usuario.business.converter.UsuarioConverter;
import com.alexiadev.usuario.business.dto.UsuarioDTO;
import com.alexiadev.usuario.infrastructure.entity.Usuario;
import com.alexiadev.usuario.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class UsuarioService {
    private final UsuarioRepository usuarioRepository; //injeção de dependência da interface UsuarioRepository

    private final UsuarioConverter usuarioConverter;

    public UsuarioDTO salvaUsuario(UsuarioDTO usuarioDTO){ //recebeu objeto usuarioDTO
        Usuario usuario = usuarioConverter.paraUsuario(usuarioDTO); //transformou em um usuario (entity)
        usuario = usuarioRepository.save(usuario); //salvou a info no banco de dados, que retorna um usuario (entity)
        return usuarioConverter.paraUsuarioDTO(usuario); //converteu novamente para usuarioDTO

        /*return usuarioConverter.paraUsuarioDTO(usuarioRepository.save(usuario)); -> mesma coisa*/
    }
}
