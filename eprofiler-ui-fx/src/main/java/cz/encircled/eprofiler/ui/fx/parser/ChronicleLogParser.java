package cz.encircled.eprofiler.ui.fx.parser;

import cz.encircled.eprofiler.ui.fx.LogEntry;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ChronicleQueueBuilder;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.queue.TailerDirection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vlad on 25-Jun-16.
 */
public class ChronicleLogParser implements LogParser {

    @Override
    public List<LogEntry> parse(String path) {
        List<LogEntry> roots = new ArrayList<>(64);
        Map<Long, LogEntry> index = new HashMap<>(256);

        ExcerptTailer tailer = getExcerptTailer(path);

        String s;
        while ((s = tailer.readText()) != null) {
            String[] split = s.split(":");
            LogEntry entry = new LogEntry();
            entry.id = Long.parseLong(split[0]);
            entry.methodName = split[2];
            entry.className = split[3];
            entry.start = Long.parseLong(split[4]);
            entry.end = Long.parseLong(split[5]);
            entry.totalTime = Long.parseLong(split[6]);
            entry.repeats = Long.parseLong(split[7]);

            if (split[1].isEmpty()) {
                roots.add(entry);
            } else {
                index.get(Long.parseLong(split[1])).children.add(entry);
            }
            index.put(entry.id, entry);
        }
        return roots;
    }

    private ExcerptTailer getExcerptTailer(String path) {
        ChronicleQueue queue = ChronicleQueueBuilder.single(path).build();
        ExcerptTailer tailer = queue.createTailer();
        tailer.direction(TailerDirection.BACKWARD);
        tailer.toEnd();
        return tailer;
    }

}
