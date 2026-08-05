package com.alexiadev.usuario.business;

import com.alexiadev.usuario.business.converter.UsuarioConverter;
import com.alexiadev.usuario.business.dto.EnderecoDTO;
import com.alexiadev.usuario.business.dto.TelefoneDTO;
import com.alexiadev.usuario.business.dto.UsuarioDTO;
import com.alexiadev.usuario.infrastructure.entity.Endereco;
import com.alexiadev.usuario.infrastructure.entity.Telefone;
import com.alexiadev.usuario.infrastructure.entity.Usuario;
import com.alexiadev.usuario.infrastructure.exceptions.ConflictException;
import com.alexiadev.usuario.infrastructure.exceptions.ResourceNotFoundException;
import com.alexiadev.usuario.infrastructure.repository.EnderecoRepository;
import com.alexiadev.usuario.infrastructure.repository.TelefoneRepository;
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
    private final EnderecoRepository enderecoRepository;
    private final TelefoneRepository telefoneRepository;



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

    public UsuarioDTO buscarUsuarioPorEmail(String email){
        try {
            return usuarioConverter.paraUsuarioDTO(
                    usuarioRepository.findByEmail(email)
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("Email não encontrado " + email)
                            )
            );
        }catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException("Email não encontrado " + email);
        }

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

    public EnderecoDTO atulizaEndereco(Long idEndereco, EnderecoDTO enderecoDTO){

        Endereco entity = enderecoRepository.findById((idEndereco)).orElseThrow(() ->
                new ResourceNotFoundException("Id não encontrado " + idEndereco));

        Endereco endereco = usuarioConverter.updateEndereco(enderecoDTO, entity);

        return usuarioConverter.paraEnderecoDTO(enderecoRepository.save(endereco));
    }

    public TelefoneDTO atualizaTelefone(Long idTelefone, TelefoneDTO dto){

        Telefone entity = telefoneRepository.findById(idTelefone).orElseThrow(() ->
                new ResourceNotFoundException("Id não encontrado " + idTelefone));

        Telefone telefone = usuarioConverter.updateTelefone(dto, entity);

        return usuarioConverter.paraTelefoneDTO(telefoneRepository.save(telefone));
    }

    public EnderecoDTO cadastraEndereco(String token, EnderecoDTO dto){
        //Pega o token e extrai o email
        String email = jwtUtil.extrairEmailToken(token.substring(7));

        //com o email, pega os dados do usuário para descobri o ID
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Email não encontrado " + email));

        //converte o Endereco dto com id em um endereço entity
        Endereco endereco = usuarioConverter.paraEnderecoEntity(dto,usuario.getId());

        //salva o endereço entity
        Endereco enderecoEntity = enderecoRepository.save(endereco);

        //retorna para controller
        return usuarioConverter.paraEnderecoDTO(enderecoEntity);
    }

    public TelefoneDTO cadastraTelefone(String token, TelefoneDTO dto){
        String email = jwtUtil.extrairEmailToken(token.substring(7));
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() ->
                new ResourceNotFoundException("Telefone não encontrado "));
        Telefone telefone = usuarioConverter.paraTelefoneEntity(dto, usuario.getId());
        Telefone telefoneEntity = telefoneRepository.save(telefone);
        return usuarioConverter.paraTelefoneDTO(telefoneEntity);

    }
}
