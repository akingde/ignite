/* @java.file.header */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal;

import org.gridgain.grid.*;
import org.gridgain.grid.compute.*;
import org.gridgain.grid.resources.*;
import org.gridgain.grid.util.lang.*;
import org.gridgain.grid.util.typedef.*;
import org.gridgain.testframework.junits.common.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static org.gridgain.grid.compute.GridComputeJobResultPolicy.*;

/**
 * Tests for {@link GridComputeTaskName} annotation.
 */
public class GridTaskNameAnnotationSelfTest extends GridCommonAbstractTest {
    /** Task name. */
    private static final String TASK_NAME = "test-task";

    /** Peer deploy aware task name. */
    private static final String PEER_DEPLOY_AWARE_TASK_NAME = "peer-deploy-aware-test-task";

    /**
     * Starts grid.
     */
    public GridTaskNameAnnotationSelfTest() {
        super(true);
    }

    /**
     * @throws Exception If failed.
     */
    public void testClass() throws Exception {
        assert grid().compute().execute(TestTask.class, null).equals(TASK_NAME);
    }

    /**
     * @throws Exception If failed.
     */
    public void testClassPeerDeployAware() throws Exception {
        assert grid().compute().execute(PeerDeployAwareTestTask.class, null).equals(PEER_DEPLOY_AWARE_TASK_NAME);
    }

    /**
     * @throws Exception If failed.
     */
    public void testInstance() throws Exception {
        assert grid().compute().execute(new TestTask(), null).equals(TASK_NAME);
    }

    /**
     * @throws Exception If failed.
     */
    public void testInstancePeerDeployAware() throws Exception {
        assert grid().compute().execute(new PeerDeployAwareTestTask(), null).
            equals(PEER_DEPLOY_AWARE_TASK_NAME);
    }

    /**
     * Test task.
     */
    @GridComputeTaskName(TASK_NAME)
    private static class TestTask implements GridComputeTask<Void, String> {
        /** {@inheritDoc} */
        @Override public Map<? extends GridComputeJob, ClusterNode> map(List<ClusterNode> subgrid,
            @Nullable Void arg) throws GridException {
            return F.asMap(new GridComputeJobAdapter() {
                @GridTaskSessionResource
                private GridComputeTaskSession ses;

                @Override public Object execute() {
                    return ses.getTaskName();
                }
            }, F.rand(subgrid));
        }

        /** {@inheritDoc} */
        @Override public GridComputeJobResultPolicy result(GridComputeJobResult res, List<GridComputeJobResult> rcvd)
            throws GridException {
            return WAIT;
        }

        /** {@inheritDoc} */
        @Override public String reduce(List<GridComputeJobResult> results) throws GridException {
            return F.first(results).getData();
        }
    }

    /**
     * Test task that implements {@link org.gridgain.grid.util.lang.GridPeerDeployAware}.
     */
    @GridComputeTaskName(PEER_DEPLOY_AWARE_TASK_NAME)
    private static class PeerDeployAwareTestTask extends TestTask implements GridPeerDeployAware {
        /** {@inheritDoc} */
        @Override public Class<?> deployClass() {
            return getClass();
        }

        /** {@inheritDoc} */
        @Override public ClassLoader classLoader() {
            return getClass().getClassLoader();
        }
    }
}
