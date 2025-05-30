package com.enonic.xp.server.impl.status;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import org.osgi.service.component.annotations.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.enonic.xp.status.StatusReporter;

@Component(immediate = true, service = StatusReporter.class)
public final class JvmInfoReporter
    extends JsonStatusReporter
{
    @Override
    public String getName()
    {
        return "jvm.info";
    }

    @Override
    public JsonNode getReport()
    {
        final ObjectNode json = JsonNodeFactory.instance.objectNode();

        final RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        json.put( "name", bean.getVmName() );
        json.put( "vendor", bean.getVmVendor() );
        json.put( "version", bean.getVmVersion() );
        json.put( "startTime", bean.getStartTime() );
        json.put( "upTime", bean.getUptime() );

        return json;
    }
}
