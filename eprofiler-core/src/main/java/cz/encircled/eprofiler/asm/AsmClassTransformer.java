package cz.encircled.eprofiler.asm;

import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.encircled.eprofiler.ProfilerAgent;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * @author Vlad on 23-May-16.
 */
public class AsmClassTransformer implements ClassFileTransformer {

    private final Matcher classMatcher;

    public AsmClassTransformer() {
        classMatcher = Pattern.compile(ProfilerAgent.getConfig().getClassNamePattern()).matcher("");
        ProfilerAgent.getWriter().info("ASM transformer created");
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        String dottedClassName = className.replaceAll("/", "\\.");

        boolean matches = classMatcher.reset(dottedClassName).matches();
        if (isNotInternal(dottedClassName) && matches) {
            ProfilerAgent.getWriter().info("Transform " + dottedClassName);

            ClassReader reader = new ClassReader(classfileBuffer);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);

            ClassVisitor rootVisitor;
            if (ProfilerAgent.getConfig().isDebug()) {
                TraceClassVisitor visitor = new TraceClassVisitor(writer, new PrintWriter(System.out));
                rootVisitor = new ClassAdapter(visitor, className);
            } else {
                rootVisitor = new ClassAdapter(writer, className);
            }

            reader.accept(rootVisitor, ClassReader.EXPAND_FRAMES);
            return writer.toByteArray();
        }

        return classfileBuffer;
    }

    private boolean isNotInternal(String dottedClassName) {
        return !dottedClassName.startsWith("cz.encircled.eprofiler") || dottedClassName.startsWith("cz.encircled.eprofiler.test");
    }

}
