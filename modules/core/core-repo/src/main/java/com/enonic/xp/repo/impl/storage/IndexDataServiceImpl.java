package com.enonic.xp.repo.impl.storage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.enonic.xp.node.Node;
import com.enonic.xp.node.NodeId;
import com.enonic.xp.node.NodeIds;
import com.enonic.xp.repo.impl.InternalContext;
import com.enonic.xp.repo.impl.ReturnFields;
import com.enonic.xp.repo.impl.ReturnValues;
import com.enonic.xp.repo.impl.StorageSource;
import com.enonic.xp.repo.impl.elasticsearch.NodeStoreDocumentFactory;
import com.enonic.xp.repo.impl.elasticsearch.document.IndexDocument;
import com.enonic.xp.repo.impl.search.SearchStorageName;
import com.enonic.xp.repo.impl.search.SearchStorageType;

@Component
public class IndexDataServiceImpl
    implements IndexDataService
{
    private final StorageDao storageDao;

    @Activate
    public IndexDataServiceImpl( @Reference final StorageDao storageDao )
    {
        this.storageDao = storageDao;
    }

    @Override
    public ReturnValues get( final NodeId nodeId, final ReturnFields returnFields, final InternalContext context )
    {
        final GetResult result = storageDao.getById( createGetByIdRequest( nodeId, returnFields, context ) );

        return result.getReturnValues();
    }

    private GetByIdRequest createGetByIdRequest( final NodeId nodeId, final ReturnFields returnFields, final InternalContext context )
    {
        return GetByIdRequest.create()
            .storageSettings( StorageSource.create()
                                  .storageType( SearchStorageType.from( context.getBranch() ) )
                                  .storageName( SearchStorageName.from( context.getRepositoryId() ) )
                                  .build() )
            .searchPreference( context.getSearchPreference() )
            .returnFields( returnFields )
            .id( nodeId.toString() )
            .build();
    }

    @Override
    public ReturnValues get( final NodeIds nodeIds, final ReturnFields returnFields, final InternalContext context )
    {
        final GetByIdsRequest getByIdsRequest = new GetByIdsRequest( context.getSearchPreference() );

        for ( final NodeId nodeId : nodeIds )
        {
            getByIdsRequest.add( createGetByIdRequest( nodeId, returnFields, context ) );
        }

        final List<GetResult> result = storageDao.getByIds( getByIdsRequest );

        final ReturnValues.Builder allResultValues = ReturnValues.create();

        for ( GetResult getResult : result )
        {
            final ReturnValues returnValues = getResult.getReturnValues();

            for ( final String key : returnValues.getReturnValues().keySet() )
            {
                allResultValues.add( key, returnValues.get( key ).getValues() );
            }
        }

        return allResultValues.build();
    }

    @Override
    public void delete( final Collection<NodeId> nodeIds, final InternalContext context )
    {
        this.storageDao.delete( DeleteRequests.create().
            settings( StorageSource.create().
                storageType( SearchStorageType.from( context.getBranch() ) ).
                storageName( SearchStorageName.from( context.getRepositoryId() ) ).
                build() ).
            ids( nodeIds.stream().map( NodeId::toString ).map( RoutableId::new ).collect( Collectors.toList() ) ).
            build() );
    }

    @Override
    public void store( final Node node, final InternalContext context )
    {
        final IndexDocument indexDocument = NodeStoreDocumentFactory.createBuilder()
            .node( node )
            .branch( context.getBranch() )
            .repositoryId( context.getRepositoryId() )
            .build()
            .create();

        this.storageDao.store( indexDocument );
    }

    @Override
    public void push( final IndexPushNodeParams pushNodeParams, final InternalContext context )
    {
        this.storageDao.copy( CopyRequest.create()
                                  .storageSettings( StorageSource.create()
                                                        .storageName( SearchStorageName.from( context.getRepositoryId() ) )
                                                        .storageType( SearchStorageType.from( context.getBranch() ) )
                                                        .build() )
                                  .nodeIds( pushNodeParams.getNodeIds() )
                                  .targetBranch( pushNodeParams.getTargetBranch() )
                                  .targetRepo( context.getRepositoryId() )
                                  .build() );
    }
}
