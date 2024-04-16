import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);  // Keep default action: pass along the filter chain
            return;
        }

        if (httpRequest.getServletPath().startsWith("/_dashboard") && httpRequest.getSession().getAttribute("employee") == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/_dashboard/login.html");
        } else if (!httpRequest.getServletPath().startsWith("/_dashboard") && httpRequest.getSession().getAttribute("user") == null) {
            httpResponse.sendRedirect("login.html");
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("api/login");
        allowedURIs.add("/_dashboard/login.html");
        allowedURIs.add("/_dashboard/login.js");
        allowedURIs.add("/api/_dashboard/login");
        allowedURIs.add("styles.css");
    }

    public void destroy() {
        // ignored.
    }

}