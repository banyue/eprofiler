package cz.encircled.eprofiler.output;

import cz.encircled.eprofiler.MethodState;
import cz.encircled.eprofiler.core.ProfilerCore;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ChronicleQueueBuilder;
import net.openhft.chronicle.queue.ExcerptAppender;

/**
 * @author Vlad on 23-May-16.
 */
public class ChronicleWriter implements OutputWriter {

    private final boolean debug;
    private ExcerptAppender appender;

    public ChronicleWriter() {
        ChronicleQueue queue = ChronicleQueueBuilder.single(ProfilerCore.config().getOutputFolder()).build();
        appender = queue.createAppender();
        debug = ProfilerCore.config().isDebug();
    }

    public void info(String message) {
        System.out.println(message);
    }

    @Override
    public void debug(String message) {
        if (debug) {
            System.out.println("profiler: " + message);
        }
    }

    @Override
    public void warn(String message) {
        System.err.println(message);
    }

    @Override
    public void flush(MethodState root) {
        log(root);
    }

    // TODO async
    private synchronized void log(MethodState state) {
        state.children.forEach(this::log);

        Long totalTime = state.totalTime();
        if (totalTime >= ProfilerCore.config().getMinDurationToLog()) {
            String message = buildMessage(state, totalTime);

            appender.writeText(message);
        }
    }

    protected String buildMessage(MethodState state, Long totalTime) {
        String parentId = state.parent == null ? "" : Long.toString(state.parent.executionId);
        return String.join(":",
                Long.toString(state.executionId),
                parentId,
                Long.toString(state.descriptor.id),
                state.descriptor.name,
                state.descriptor.arguments,
                state.descriptor.returnType,
                state.descriptor.className,
                Long.toString(state.starts.get(0)),
                Long.toString(state.ends.get(0)),
                Long.toString(totalTime),
                Long.toString(state.repeats),
                Long.toString(state.consumedCpu / 1000L),
                Long.toString(state.consumedMemory),
                Integer.toString(state.threadCount)
        );
    }

}
