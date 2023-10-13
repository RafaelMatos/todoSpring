package br.com.rafael.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.rafael.todolist.user.IUserRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    var servletPath = request.getServletPath();
    if (servletPath.startsWith("/tasks/")) {
      // Pegar a autenticação (usuário e senha)
      var autorization = request.getHeader("Authorization");

      System.out.println("autorization:");
      System.out.println(autorization);

      var authEncoded = autorization.substring("Basic".length()).trim();

      System.out.println("authEncoded:");
      System.out.println(authEncoded);

      byte[] authDecoded = Base64.getDecoder().decode(authEncoded);

      System.out.println("authDecoded:");
      System.out.println(authDecoded);

      var authString = new String(authDecoded);

      System.out.println("authString:");
      System.out.println(authString);

      String[] credentials = authString.split(":");

      System.out.println("credentials:");

      if (credentials.length == 0) {
        response.sendError(401);
      } else {
        String username = credentials[0];
        String password = credentials[1];

        // Validar usuário
        var user = this.userRepository.findByUsername(username);
        if (user == null) {
          response.sendError(401);
        } else {
          var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());

          if (passwordVerify.verified) {
            request.setAttribute("idUser", user.getId());
            filterChain.doFilter(request, response);
          } else {
            response.sendError(401);
          }
        }
      }

    } else {
      filterChain.doFilter(request, response);
    }

  }

}
