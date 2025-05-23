package com.enonic.xp.repo.impl.repository;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.ImmutableSet;

import com.enonic.xp.branch.Branch;
import com.enonic.xp.branch.Branches;
import com.enonic.xp.data.PropertySet;
import com.enonic.xp.data.PropertyTree;
import com.enonic.xp.index.ChildOrder;
import com.enonic.xp.index.IndexType;
import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodeEditor;
import com.enonic.xp.node.NodeId;
import com.enonic.xp.repo.impl.index.IndexMapping;
import com.enonic.xp.repo.impl.index.IndexSettings;
import com.enonic.xp.repository.RepositoryConstants;
import com.enonic.xp.repository.RepositoryId;
import com.enonic.xp.security.SystemConstants;

public class RepositoryNodeTranslator
{
    private static final String BRANCHES_KEY = "branches";

    private static final String INDEX_CONFIG_KEY = "indexConfigs";

    private static final String MAPPING_KEY = "mapping";

    private static final String SETTINGS_KEY = "settings";

    private static final String DATA_KEY = "data";

    private static final String TRANSIENT_KEY = "transient";

    public static Node toNode( final RepositoryEntry repository )
    {
        final PropertyTree repositoryNodeData = new PropertyTree();
        toNodeData( repository.getBranches(), repositoryNodeData );
        final RepositorySettings repositorySettings = repository.getSettings();
        toNodeData( repositorySettings.getIndexDefinitions(), repositoryNodeData );
        toNodeData( repository.getData(), repositoryNodeData );
        if ( !SystemConstants.SYSTEM_REPO_ID.equals( repository.getId() ) && repository.isTransient() )
        {
            repositoryNodeData.setBoolean( TRANSIENT_KEY, true );
        }

        return Node.create().
            id( NodeId.from( repository.getId() ) ).
            childOrder( ChildOrder.defaultOrder() ).
            data( repositoryNodeData ).
            name( repository.getId().toString() ).
            parentPath( RepositoryConstants.REPOSITORY_STORAGE_PARENT_PATH ).
            permissions( SystemConstants.SYSTEM_REPO_DEFAULT_ACL ).
            attachedBinaries( repository.getAttachments() ).
            build();
    }

    public static NodeEditor toCreateBranchNodeEditor( final Branch branch )
    {
        return toBeEdited -> toBeEdited.data.addString( BRANCHES_KEY, branch.getValue() );
    }

    public static NodeEditor toUpdateRepositoryNodeEditor( UpdateRepositoryEntryParams params )
    {
        return toBeEdited -> {
            if ( !SystemConstants.SYSTEM_REPO_ID.equals( params.getRepositoryId() ) )
            {
                if ( params.isTransient() )
                {
                    toBeEdited.data.setBoolean( TRANSIENT_KEY, true );
                }
                else
                {
                    toBeEdited.data.removeProperty( TRANSIENT_KEY );
                }
            }
            toBeEdited.data.setSet( DATA_KEY, params.getRepositoryData().getRoot().copy( toBeEdited.data ) );
        };
    }

    public static NodeEditor toDeleteBranchNodeEditor( final Branch branch )
    {
        return toBeEdited -> {
            final Iterable<String> branches = toBeEdited.data.getStrings( BRANCHES_KEY );

            toBeEdited.data.removeProperties( BRANCHES_KEY );
            for ( Iterator<String> branchIterator = branches.iterator(); branchIterator.hasNext(); )
            {
                final String currentBranch = branchIterator.next();
                if ( !branch.getValue().equals( currentBranch ) )
                {
                    toBeEdited.data.addString( BRANCHES_KEY, currentBranch );
                }
            }
        };
    }

    private static void toNodeData( final Branches branches, final PropertyTree data )
    {
        branches.forEach( branch -> data.addString( BRANCHES_KEY, branch.getValue() ) );
    }

    private static void toNodeData( final PropertyTree repositoryData, final PropertyTree data )
    {
        data.addSet( DATA_KEY, repositoryData.getRoot().copy( data.getRoot().getTree() ) );
    }

    private static void toNodeData( final IndexDefinitions indexDefinitions, final PropertyTree data )
    {
        if ( indexDefinitions != null )
        {
            final PropertySet indexConfigsPropertySet = data.addSet( INDEX_CONFIG_KEY );
            for ( IndexType indexType : IndexType.values() )
            {
                final IndexDefinition indexDefinition = indexDefinitions.get( indexType );
                if ( indexDefinition != null )
                {
                    final PropertySet indexConfigPropertySet = indexConfigsPropertySet.addSet( indexType.getName() );
                    final IndexMapping indexMapping = indexDefinition.getMapping();
                    if ( indexMapping != null )
                    {
                        indexConfigPropertySet.setSet( MAPPING_KEY, PropertyTree.fromMap( indexMapping.getData() ).getRoot().copy( data ) );
                    }

                    final IndexSettings indexSettings = indexDefinition.getSettings();
                    if ( indexSettings != null )
                    {
                        indexConfigPropertySet.setSet( SETTINGS_KEY, PropertyTree.fromMap( indexSettings.getData() ).getRoot().copy( data ) );
                    }
                }
            }
        }
    }

    public static RepositoryEntry toRepository( final Node node )
    {
        final PropertyTree nodeData = node.data();

        final PropertyTree repositoryData = toRepositoryData( nodeData );

        return RepositoryEntry.create().
            id( RepositoryId.from( node.id().toString() ) ).
            branches( toBranches( nodeData ) ).
            settings( toRepositorySettings( nodeData ) ).
            data( repositoryData ).
            attachments( node.getAttachedBinaries() ).
            transientFlag( Objects.requireNonNullElse( nodeData.getBoolean( TRANSIENT_KEY ), false ) ).
            build();
    }

    private static Branches toBranches( final PropertyTree nodeData )
    {
        final ImmutableSet.Builder<Branch> branches = ImmutableSet.builder();
        nodeData.getStrings( BRANCHES_KEY ).
            forEach( branchValue -> branches.add( Branch.from( branchValue ) ) );
        return Branches.from( branches.build() );
    }

    public static RepositorySettings toRepositorySettings( final PropertyTree nodeData )
    {
        final PropertySet indexConfigsSet = nodeData.getSet( INDEX_CONFIG_KEY );
        if ( indexConfigsSet != null )
        {
            final IndexDefinitions.Builder indexConfigs = IndexDefinitions.create();
            for ( IndexType indexType : IndexType.values() )
            {
                final PropertySet indexConfigSet = indexConfigsSet.getSet( indexType.getName() );
                if ( indexConfigSet != null )
                {
                    final IndexDefinition.Builder indexConfig = IndexDefinition.create();

                    final PropertySet mappingSet = indexConfigSet.getSet( MAPPING_KEY );
                    indexConfig.mapping( mappingSet == null ? null : IndexMapping.from( mappingSet.toTree().toMap() ) );

                    final PropertySet settingsSet = indexConfigSet.getSet( SETTINGS_KEY );
                    indexConfig.settings( settingsSet == null ? null : IndexSettings.from( settingsSet.toTree().toMap() ) );

                    indexConfigs.add( indexType, indexConfig.build() );
                }
            }
            return RepositorySettings.create().indexDefinitions( indexConfigs.build() ).build();
        }
        return null;
    }

    private static PropertyTree toRepositoryData( final PropertyTree nodeData )
    {
        return Optional.ofNullable( nodeData.getSet( DATA_KEY ) ).map( PropertySet::toTree ).orElse( null );
    }
}
