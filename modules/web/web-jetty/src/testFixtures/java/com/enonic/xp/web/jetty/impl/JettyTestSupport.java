package com.enonic.xp.web.jetty.impl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServlet;

import static org.mockito.Mockito.mock;

public abstract class JettyTestSupport
{
    protected JettyTestServer server;

    protected HttpClient client;

    protected String baseUrl;

    protected JettyConfig config;

    @BeforeEach
    public final void startServer()
        throws Exception
    {
        this.config = mock( JettyConfig.class, invocation -> invocation.getMethod().getDefaultValue() );
        this.server = new JettyTestServer();
        this.server.start();
        configure();

        this.client = HttpClient.newHttpClient();
        this.baseUrl = "http://localhost:" + this.server.getPort();
    }

    protected abstract void configure()
        throws Exception;

    protected void destroy()
        throws Exception
    {
        // Do nothing
    }

    @AfterEach
    public final void stopServer()
        throws Exception
    {
        destroy();
        this.server.stop();
    }

    protected final void addFilter( final Filter filter, final String mapping )
    {
        this.server.addFilter( filter, mapping );
    }

    protected final void addServlet( final HttpServlet servlet, final String mapping )
    {
        this.server.addServlet( servlet, mapping );
    }

    protected final HttpRequest.Builder newRequest( final String path )
    {
        return HttpRequest.newBuilder( URI.create( this.baseUrl + path ) );
    }

    protected final HttpResponse callRequest( final HttpRequest request )
        throws Exception
    {
        return this.client.send( request, HttpResponse.BodyHandlers.ofString() );
    }
}
