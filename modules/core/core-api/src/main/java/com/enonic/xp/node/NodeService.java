package com.enonic.xp.node;

import com.google.common.io.ByteSource;

import com.enonic.xp.annotation.PublicApi;
import com.enonic.xp.blob.NodeVersionKey;
import com.enonic.xp.branch.Branch;
import com.enonic.xp.security.acl.AccessControlList;
import com.enonic.xp.util.BinaryReference;

@PublicApi
public interface NodeService
{
    Node create( CreateNodeParams params );

    Node update( UpdateNodeParams params );

    PatchNodeResult patch( PatchNodeParams params );

    Node rename( RenameNodeParams params );

    PushNodesResult push( NodeIds ids, Branch target );

    PushNodesResult push( NodeIds ids, Branch target, PushNodesListener pushListener );

    @Deprecated
    NodeIds deleteById( NodeId id );

    @Deprecated
    NodeIds deleteById( NodeId id, DeleteNodeListener deleteListener );

    @Deprecated
    NodeIds deleteByPath( NodePath path );

    DeleteNodeResult delete( DeleteNodeParams deleteNodeParams );

    Node getById( NodeId id );

    Node getByIdAndVersionId( NodeId id, NodeVersionId versionId );

    Nodes getByIds( NodeIds ids );

    Node getByPath( NodePath path );

    @Deprecated
    Node getByPathAndVersionId( NodePath path, NodeVersionId versionId );

    Nodes getByPaths( NodePaths paths );

    Node duplicate( DuplicateNodeParams params );

    @Deprecated
    Node move( NodeId nodeId, NodePath parentNodePath, MoveNodeListener moveListener );

    Node move( MoveNodeParams params );

    @Deprecated
    Nodes move( NodeIds nodeIds, NodePath parentNodePath, MoveNodeListener moveListener );

    FindNodesByParentResult findByParent( FindNodesByParentParams params );

    FindNodesByQueryResult findByQuery( NodeQuery nodeQuery );

    @Deprecated
    FindNodePathsByQueryResult findNodePathsByQuery( NodeQuery nodeQuery );

    FindNodesByMultiRepoQueryResult findByQuery( MultiRepoNodeQuery nodeQuery );

    NodeComparison compare( NodeId id, Branch target );

    NodeComparisons compare( NodeIds ids, Branch target );

    NodeVersionQueryResult findVersions( GetNodeVersionsParams params );

    NodeVersionQueryResult findVersions( NodeVersionQuery nodeVersionQuery );

    NodeCommitQueryResult findCommits( NodeCommitQuery nodeCommitQuery );

    @Deprecated
    boolean deleteVersion( NodeId nodeId, NodeVersionId nodeVersionId );

    GetActiveNodeVersionsResult getActiveVersions( GetActiveNodeVersionsParams params );

    NodeVersionId setActiveVersion( NodeId nodeId, NodeVersionId nodeVersionId );

    Node setChildOrder( SetNodeChildOrderParams params );

    ReorderChildNodesResult reorderChildren( ReorderChildNodesParams params );

    NodeVersion getByNodeVersionKey( NodeVersionKey nodeVersionKey );

    ResolveSyncWorkResult resolveSyncWork( SyncWorkResolverParams params );

    void refresh( RefreshMode refreshMode );

    ApplyNodePermissionsResult applyPermissions( ApplyNodePermissionsParams params );

    ByteSource getBinary( NodeId nodeId, BinaryReference reference );

    ByteSource getBinary( NodeId nodeId, NodeVersionId nodeVersionId, BinaryReference reference );

    String getBinaryKey( NodeId nodeId, BinaryReference reference );

    @Deprecated
    Node createRootNode( CreateRootNodeParams params );

    @Deprecated
    SetNodeStateResult setNodeState( SetNodeStateParams params );

    Node getRoot();

    @Deprecated
    Node setRootPermissions( AccessControlList accessControlList );

    ImportNodeResult importNode( ImportNodeParams params );

    LoadNodeResult loadNode( LoadNodeParams params );

    @Deprecated
    NodesHasChildrenResult hasChildren( Nodes nodes );

    NodeCommitEntry commit( NodeCommitEntry nodeCommitEntry, RoutableNodeVersionIds routableNodeVersionIds );

    NodeCommitEntry commit( NodeCommitEntry nodeCommitEntry, NodeIds nodeIds );

    NodeCommitEntry getCommit( NodeCommitId nodeCommitId );

    boolean hasChildren( Node node );

    boolean nodeExists( NodeId nodeId );

    boolean nodeExists( NodePath nodePath );

    boolean hasUnpublishedChildren( NodeId parent, Branch target );

    void importNodeVersion( ImportNodeVersionParams params );

    void importNodeCommit( ImportNodeCommitParams params );

}
