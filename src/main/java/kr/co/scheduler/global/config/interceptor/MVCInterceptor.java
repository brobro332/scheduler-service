package kr.co.scheduler.global.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
public class MVCInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        Object img = modelAndView.getModel().get("img");
        Object info = modelAndView.getModel().get("info");
        Object alerts = modelAndView.getModel().get("alerts");

        if (img != null) {

            request.getSession().setAttribute("img", img);
        }

        if (info != null) {

            request.getSession().setAttribute("info", info);
        }

        if (alerts != null) {

            request.getSession().setAttribute("alerts", alerts);
        }
    }
}
