package com.enonic.xp.portal.impl.macro;

import com.enonic.xp.app.ApplicationKey;
import com.enonic.xp.portal.PortalRequest;
import com.enonic.xp.portal.PortalRequestAccessor;
import com.enonic.xp.portal.PortalResponse;
import com.enonic.xp.portal.impl.controller.PortalResponseSerializer;
import com.enonic.xp.portal.macro.MacroContext;
import com.enonic.xp.portal.macro.MacroProcessor;
import com.enonic.xp.script.ScriptExports;
import com.enonic.xp.script.ScriptValue;

public final class MacroProcessorScript
    implements MacroProcessor
{
    private static final String SCRIPT_METHOD_NAME = "macro";

    private final ScriptExports scriptExports;

    public MacroProcessorScript( final ScriptExports scriptExports )
    {
        this.scriptExports = scriptExports;
    }

    @Override
    public PortalResponse process( final MacroContext macroContext )
    {
        final PortalRequest portalRequest = macroContext.getRequest();
        final ApplicationKey previousAppKey = portalRequest.getApplicationKey();
        portalRequest.setApplicationKey( scriptExports.getScript().getApplicationKey() );
        PortalRequestAccessor.set( portalRequest );
        try
        {
            return doProcess( macroContext );
        }
        finally
        {
            PortalRequestAccessor.remove();
            portalRequest.setApplicationKey( previousAppKey );
        }
    }

    private PortalResponse doProcess( final MacroContext macroContext )
    {
        final boolean exists = this.scriptExports.hasMethod( SCRIPT_METHOD_NAME );
        if ( !exists )
        {
            return null;
        }

        final MacroContextMapper macroContextMapper = new MacroContextMapper( macroContext );
        final ScriptValue result = this.scriptExports.executeMethod( SCRIPT_METHOD_NAME, macroContextMapper );
        return new PortalResponseSerializer( result ).serialize();
    }

}
