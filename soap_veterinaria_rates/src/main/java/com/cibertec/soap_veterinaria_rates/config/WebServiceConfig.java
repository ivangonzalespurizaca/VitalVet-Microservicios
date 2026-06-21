package com.cibertec.soap_veterinaria_rates.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet() {
            @Override
            protected void doService(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response)
                    throws Exception {

                response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
                response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS, GET");

                response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, SOAPAction, Authorization, X-Requested-With");

                if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                    response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_OK);
                    return;
                }

                super.doService(request, response);
            }
        };

        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean(name = "comentarios")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema helloSchema) {
        DefaultWsdl11Definition definition = new DefaultWsdl11Definition();
        definition.setPortTypeName("ComentariosPort");
        definition.setTargetNamespace("http://example.com/veterinaria-vitalvet/comentarios");
        definition.setLocationUri("/ws");
        definition.setSchema(helloSchema);
        return definition;
    }

   @Bean
    public XsdSchema helloSchema() {
        return new SimpleXsdSchema(new ClassPathResource("wsdl/comentario.xsd"));
    }




}
