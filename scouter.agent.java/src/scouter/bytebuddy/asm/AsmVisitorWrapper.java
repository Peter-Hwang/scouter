// Generated by delombok at Sun Feb 26 12:31:38 KST 2017
package scouter.bytebuddy.asm;

import scouter.bytebuddy.description.field.FieldDescription;
import scouter.bytebuddy.description.field.FieldList;
import scouter.bytebuddy.description.method.MethodDescription;
import scouter.bytebuddy.description.method.MethodList;
import scouter.bytebuddy.description.type.TypeDescription;
import scouter.bytebuddy.dynamic.DynamicType;
import scouter.bytebuddy.implementation.Implementation;
import scouter.bytebuddy.jar.asm.*;
import scouter.bytebuddy.matcher.ElementMatcher;
import scouter.bytebuddy.pool.TypePool;
import scouter.bytebuddy.utility.CompoundList;
import scouter.bytebuddy.jar.asm.ClassVisitor;
import scouter.bytebuddy.implementation.auxiliary.AuxiliaryType;

import java.util.*;

/**
 * A class visitor wrapper is used in order to register an intermediate ASM {@link ClassVisitor} which
 * is applied to the main type created by a {@link DynamicType.Builder} but not
 * to any {@link AuxiliaryType}s, if any.
 */
public interface AsmVisitorWrapper {
    /**
     * Indicates that no flags should be set.
     */
    int NO_FLAGS = 0;

    /**
     * Defines the flags that are provided to any {@code ClassWriter} when writing a class. Typically, this gives opportunity to instruct ASM
     * to compute stack map frames or the size of the local variables array and the operand stack. If no specific flags are required for
     * applying this wrapper, the given value is to be returned.
     *
     * @param flags The currently set flags. This value should be combined (e.g. {@code flags | foo}) into the value that is returned by this wrapper.
     * @return The flags to be provided to the ASM {@code ClassWriter}.
     */
    int mergeWriter(int flags);

    /**
     * Defines the flags that are provided to any {@code ClassReader} when reading a class if applicable. Typically, this gives opportunity to
     * instruct ASM to expand or skip frames and to skip code and debug information. If no specific flags are required for applying this
     * wrapper, the given value is to be returned.
     *
     * @param flags The currently set flags. This value should be combined (e.g. {@code flags | foo}) into the value that is returned by this wrapper.
     * @return The flags to be provided to the ASM {@code ClassReader}.
     */
    int mergeReader(int flags);

    /**
     * Applies a {@code ClassVisitorWrapper} to the creation of a {@link DynamicType}.
     *
     * @param instrumentedType      The instrumented type.
     * @param classVisitor          A {@code ClassVisitor} to become the new primary class visitor to which the created
     * {@link DynamicType} is written to.
     * @param implementationContext The implementation context of the current instrumentation.
     * @param typePool              The type pool that was provided for the class creation.
     * @param fields                The instrumented type's fields.
     * @param methods               The instrumented type's methods non-ingored declared and virtually inherited methods.
     * @param writerFlags           The ASM {@link ClassWriter} flags to consider.
     * @param readerFlags           The ASM {@link ClassReader} flags to consider.
     * @return A new {@code ClassVisitor} that usually delegates to the {@code ClassVisitor} delivered in the argument.
     */
    ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor, Implementation.Context implementationContext, TypePool typePool, FieldList<FieldDescription.InDefinedShape> fields, MethodList<?> methods, int writerFlags, int readerFlags);


    /**
     * A class visitor wrapper that does not apply any changes.
     */
    enum NoOp implements AsmVisitorWrapper {
        /**
         * The singleton instance.
         */
        INSTANCE;

        @Override
        public int mergeWriter(int flags) {
            return flags;
        }

        @Override
        public int mergeReader(int flags) {
            return flags;
        }

        @Override
        public ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor, Implementation.Context implementationContext, TypePool typePool, FieldList<FieldDescription.InDefinedShape> fields, MethodList<?> methods, int writerFlags, int readerFlags) {
            return classVisitor;
        }
    }


    /**
     * An abstract base implementation of an ASM visitor wrapper that does not set any flags.
     */
    abstract class AbstractBase implements AsmVisitorWrapper {
        @Override
        public int mergeWriter(int flags) {
            return flags;
        }

        @Override
        public int mergeReader(int flags) {
            return flags;
        }
    }


    /**
     * An ASM visitor wrapper that allows to wrap declared fields of the instrumented type with a {@link FieldVisitorWrapper}.
     */
    class ForDeclaredFields extends AbstractBase {
        /**
         * The list of entries that describe matched fields in their application order.
         */
        private final List<Entry> entries;

        /**
         * Creates a new visitor wrapper for declared fields.
         */
        public ForDeclaredFields() {
            this(Collections.<Entry>emptyList());
        }

        /**
         * Creates a new visitor wrapper for declared fields.
         *
         * @param entries The list of entries that describe matched fields in their application order.
         */
        protected ForDeclaredFields(List<Entry> entries) {
            this.entries = entries;
        }

        /**
         * Defines a new field visitor wrapper to be applied if the given field matcher is matched. Previously defined
         * entries are applied before the given matcher is applied.
         *
         * @param matcher             The matcher to identify fields to be wrapped.
         * @param fieldVisitorWrapper The field visitor wrapper to be applied if the given matcher is matched.
         * @return A new ASM visitor wrapper that applied the given field visitor wrapper if the supplied matcher is matched.
         */
        public ForDeclaredFields field(ElementMatcher<? super FieldDescription.InDefinedShape> matcher, FieldVisitorWrapper... fieldVisitorWrapper) {
            return field(matcher, Arrays.asList(fieldVisitorWrapper));
        }

        /**
         * Defines a new field visitor wrapper to be applied if the given field matcher is matched. Previously defined
         * entries are applied before the given matcher is applied.
         *
         * @param matcher              The matcher to identify fields to be wrapped.
         * @param fieldVisitorWrappers The field visitor wrapper to be applied if the given matcher is matched.
         * @return A new ASM visitor wrapper that applied the given field visitor wrapper if the supplied matcher is matched.
         */
        public ForDeclaredFields field(ElementMatcher<? super FieldDescription.InDefinedShape> matcher, List<? extends FieldVisitorWrapper> fieldVisitorWrappers) {
            return new ForDeclaredFields(CompoundList.of(entries, new Entry(matcher, fieldVisitorWrappers)));
        }

        @Override
        public ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor, Implementation.Context implementationContext, TypePool typePool, FieldList<FieldDescription.InDefinedShape> fields, MethodList<?> methods, int writerFlags, int readerFlags) {
            return new DispatchingVisitor(classVisitor, instrumentedType, fields);
        }


        /**
         * A field visitor wrapper that allows for wrapping a {@link FieldVisitor} defining a declared field.
         */
        public interface FieldVisitorWrapper {
            /**
             * Wraps a field visitor.
             *
             * @param instrumentedType The instrumented type.
             * @param fieldDescription The field that is currently being defined.
             * @param fieldVisitor     The original field visitor that defines the given field.
             * @return The wrapped field visitor.
             */
            FieldVisitor wrap(TypeDescription instrumentedType, FieldDescription.InDefinedShape fieldDescription, FieldVisitor fieldVisitor);
        }


        /**
         * An entry describing a field visitor wrapper paired with a matcher for fields to be wrapped.
         */
        protected static class Entry implements ElementMatcher<FieldDescription.InDefinedShape>, FieldVisitorWrapper {
            /**
             * The matcher to identify fields to be wrapped.
             */
            private final ElementMatcher<? super FieldDescription.InDefinedShape> matcher;
            /**
             * The field visitor wrapper to be applied if the given matcher is matched.
             */
            private final List<? extends FieldVisitorWrapper> fieldVisitorWrappers;

            /**
             * Creates a new entry.
             *
             * @param matcher              The matcher to identify fields to be wrapped.
             * @param fieldVisitorWrappers The field visitor wrapper to be applied if the given matcher is matched.
             */
            protected Entry(ElementMatcher<? super FieldDescription.InDefinedShape> matcher, List<? extends FieldVisitorWrapper> fieldVisitorWrappers) {
                this.matcher = matcher;
                this.fieldVisitorWrappers = fieldVisitorWrappers;
            }

            @Override
            public boolean matches(FieldDescription.InDefinedShape target) {
                return target != null && matcher.matches(target);
            }

            @Override
            public FieldVisitor wrap(TypeDescription instrumentedType, FieldDescription.InDefinedShape fieldDescription, FieldVisitor fieldVisitor) {
                for (FieldVisitorWrapper fieldVisitorWrapper : fieldVisitorWrappers) {
                    fieldVisitor = fieldVisitorWrapper.wrap(instrumentedType, fieldDescription, fieldVisitor);
                }
                return fieldVisitor;
            }

            @java.lang.Override
            @java.lang.SuppressWarnings("all")
            @javax.annotation.Generated("lombok")
            public boolean equals(final java.lang.Object o) {
                if (o == this) return true;
                if (!(o instanceof AsmVisitorWrapper.ForDeclaredFields.Entry)) return false;
                final AsmVisitorWrapper.ForDeclaredFields.Entry other = (AsmVisitorWrapper.ForDeclaredFields.Entry) o;
                if (!other.canEqual((java.lang.Object) this)) return false;
                final java.lang.Object this$matcher = this.matcher;
                final java.lang.Object other$matcher = other.matcher;
                if (this$matcher == null ? other$matcher != null : !this$matcher.equals(other$matcher)) return false;
                final java.lang.Object this$fieldVisitorWrappers = this.fieldVisitorWrappers;
                final java.lang.Object other$fieldVisitorWrappers = other.fieldVisitorWrappers;
                if (this$fieldVisitorWrappers == null ? other$fieldVisitorWrappers != null : !this$fieldVisitorWrappers.equals(other$fieldVisitorWrappers)) return false;
                return true;
            }

            @java.lang.SuppressWarnings("all")
            @javax.annotation.Generated("lombok")
            protected boolean canEqual(final java.lang.Object other) {
                return other instanceof AsmVisitorWrapper.ForDeclaredFields.Entry;
            }

            @java.lang.Override
            @java.lang.SuppressWarnings("all")
            @javax.annotation.Generated("lombok")
            public int hashCode() {
                final int PRIME = 59;
                int result = 1;
                final java.lang.Object $matcher = this.matcher;
                result = result * PRIME + ($matcher == null ? 43 : $matcher.hashCode());
                final java.lang.Object $fieldVisitorWrappers = this.fieldVisitorWrappers;
                result = result * PRIME + ($fieldVisitorWrappers == null ? 43 : $fieldVisitorWrappers.hashCode());
                return result;
            }
        }


        /**
         * A class visitor that applies the outer ASM visitor for identifying declared fields.
         */
        protected class DispatchingVisitor extends ClassVisitor {
            /**
             * The instrumented type.
             */
            private final TypeDescription instrumentedType;
            /**
             * A mapping of fields by their name and descriptor key-combination.
             */
            private final Map<String, FieldDescription.InDefinedShape> knownFields;

            /**
             * Creates a new dispatching visitor.
             *
             * @param classVisitor     The underlying class visitor.
             * @param instrumentedType The instrumented type.
             * @param fields           The instrumented type's declared fields.
             */
            protected DispatchingVisitor(ClassVisitor classVisitor, TypeDescription instrumentedType, FieldList<FieldDescription.InDefinedShape> fields) {
                super(Opcodes.ASM5, classVisitor);
                this.instrumentedType = instrumentedType;
                knownFields = new HashMap<String, FieldDescription.InDefinedShape>();
                for (FieldDescription.InDefinedShape fieldDescription : fields) {
                    knownFields.put(fieldDescription.getInternalName() + fieldDescription.getDescriptor(), fieldDescription);
                }
            }

            @Override
            public FieldVisitor visitField(int modifiers, String internalName, String descriptor, String signature, Object defaultValue) {
                FieldVisitor fieldVisitor = super.visitField(modifiers, internalName, descriptor, signature, defaultValue);
                FieldDescription.InDefinedShape fieldDescription = knownFields.get(internalName + descriptor);
                for (Entry entry : entries) {
                    if (entry.matches(fieldDescription)) {
                        fieldVisitor = entry.wrap(instrumentedType, fieldDescription, fieldVisitor);
                    }
                }
                return fieldVisitor;
            }
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof AsmVisitorWrapper.ForDeclaredFields)) return false;
            final AsmVisitorWrapper.ForDeclaredFields other = (AsmVisitorWrapper.ForDeclaredFields) o;
            if (!other.canEqual((java.lang.Object) this)) return false;
            final java.lang.Object this$entries = this.entries;
            final java.lang.Object other$entries = other.entries;
            if (this$entries == null ? other$entries != null : !this$entries.equals(other$entries)) return false;
            return true;
        }

        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        protected boolean canEqual(final java.lang.Object other) {
            return other instanceof AsmVisitorWrapper.ForDeclaredFields;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final java.lang.Object $entries = this.entries;
            result = result * PRIME + ($entries == null ? 43 : $entries.hashCode());
            return result;
        }
    }


    /**
     * <p>
     * An ASM visitor wrapper that allows to wrap <b>declared methods</b> of the instrumented type with a {@link MethodVisitorWrapper}.
     * </p>
     * <p>
     * Note: Inherited methods are <b>not</b> matched by this visitor, even if they are intercepted by a normal interception.
     * </p>
     */
    class ForDeclaredMethods implements AsmVisitorWrapper {
        /**
         * The list of entries that describe matched methods in their application order.
         */
        private final List<Entry> entries;
        /**
         * The writer flags to set.
         */
        private final int writerFlags;
        /**
         * The reader flags to set.
         */
        private final int readerFlags;

        /**
         * Creates a new visitor wrapper for declared methods.
         */
        public ForDeclaredMethods() {
            this(Collections.<Entry>emptyList(), NO_FLAGS, NO_FLAGS);
        }

        /**
         * Creates a new visitor wrapper for declared methods.
         *
         * @param entries     The list of entries that describe matched methods in their application order.
         * @param readerFlags The reader flags to set.
         * @param writerFlags The writer flags to set.
         */
        protected ForDeclaredMethods(List<Entry> entries, int writerFlags, int readerFlags) {
            this.entries = entries;
            this.writerFlags = writerFlags;
            this.readerFlags = readerFlags;
        }

        /**
         * Defines a new method visitor wrapper to be applied if the given method matcher is matched. Previously defined
         * entries are applied before the given matcher is applied.
         *
         * @param matcher              The matcher to identify methods to be wrapped.
         * @param methodVisitorWrapper The method visitor wrapper to be applied if the given matcher is matched.
         * @return A new ASM visitor wrapper that applied the given method visitor wrapper if the supplied matcher is matched.
         */
        public ForDeclaredMethods method(ElementMatcher<? super MethodDescription> matcher, MethodVisitorWrapper... methodVisitorWrapper) {
            return method(matcher, Arrays.asList(methodVisitorWrapper));
        }

        /**
         * Defines a new method visitor wrapper to be applied if the given method matcher is matched. Previously defined
         * entries are applied before the given matcher is applied.
         *
         * @param matcher               The matcher to identify methods to be wrapped.
         * @param methodVisitorWrappers The method visitor wrapper to be applied if the given matcher is matched.
         * @return A new ASM visitor wrapper that applied the given method visitor wrapper if the supplied matcher is matched.
         */
        public ForDeclaredMethods method(ElementMatcher<? super MethodDescription> matcher, List<? extends MethodVisitorWrapper> methodVisitorWrappers) {
            return new ForDeclaredMethods(CompoundList.of(entries, new Entry(matcher, methodVisitorWrappers)), writerFlags, readerFlags);
        }

        /**
         * Sets flags for the {@link ClassWriter} this wrapper is applied to.
         *
         * @param flags The flags to set for the {@link ClassWriter}.
         * @return A new ASM visitor wrapper that sets the supplied writer flags.
         */
        public ForDeclaredMethods writerFlags(int flags) {
            return new ForDeclaredMethods(entries, writerFlags | flags, readerFlags);
        }

        /**
         * Sets flags for the {@link ClassReader} this wrapper is applied to.
         *
         * @param flags The flags to set for the {@link ClassReader}.
         * @return A new ASM visitor wrapper that sets the supplied reader flags.
         */
        public ForDeclaredMethods readerFlags(int flags) {
            return new ForDeclaredMethods(entries, writerFlags, readerFlags | flags);
        }

        @Override
        public int mergeWriter(int flags) {
            return flags | writerFlags;
        }

        @Override
        public int mergeReader(int flags) {
            return flags | readerFlags;
        }

        @Override
        public ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor, Implementation.Context implementationContext, TypePool typePool, FieldList<FieldDescription.InDefinedShape> fields, MethodList<?> methods, int writerFlags, int readerFlags) {
            return new DispatchingVisitor(classVisitor, instrumentedType, implementationContext, typePool, methods, writerFlags, readerFlags);
        }


        /**
         * A method visitor wrapper that allows for wrapping a {@link MethodVisitor} defining a declared method.
         */
        public interface MethodVisitorWrapper {
            /**
             * Wraps a method visitor.
             *
             * @param instrumentedType      The instrumented type.
             * @param instrumentedMethod    The method that is currently being defined.
             * @param methodVisitor         The original field visitor that defines the given method.
             * @param implementationContext The implementation context to use.
             * @param typePool              The type pool to use.
             * @param writerFlags           The ASM {@link ClassWriter} reader flags to consider.
             * @param readerFlags           The ASM {@link ClassReader} reader flags to consider.
             * @return The wrapped method visitor.
             */
            MethodVisitor wrap(TypeDescription instrumentedType, MethodDescription instrumentedMethod, MethodVisitor methodVisitor, Implementation.Context implementationContext, TypePool typePool, int writerFlags, int readerFlags);
        }


        /**
         * An entry describing a method visitor wrapper paired with a matcher for fields to be wrapped.
         */
        protected static class Entry implements ElementMatcher<MethodDescription>, MethodVisitorWrapper {
            /**
             * The matcher to identify methods to be wrapped.
             */
            private final ElementMatcher<? super MethodDescription> matcher;
            /**
             * The method visitor wrapper to be applied if the given matcher is matched.
             */
            private final List<? extends MethodVisitorWrapper> methodVisitorWrappers;

            /**
             * Creates a new entry.
             *
             * @param matcher               The matcher to identify methods to be wrapped.
             * @param methodVisitorWrappers The method visitor wrapper to be applied if the given matcher is matched.
             */
            protected Entry(ElementMatcher<? super MethodDescription> matcher, List<? extends MethodVisitorWrapper> methodVisitorWrappers) {
                this.matcher = matcher;
                this.methodVisitorWrappers = methodVisitorWrappers;
            }

            @Override
            public boolean matches(MethodDescription target) {
                return target != null && matcher.matches(target);
            }

            @Override
            public MethodVisitor wrap(TypeDescription instrumentedType, MethodDescription instrumentedMethod, MethodVisitor methodVisitor, Implementation.Context implementationContext, TypePool typePool, int writerFlags, int readerFlags) {
                for (MethodVisitorWrapper methodVisitorWrapper : methodVisitorWrappers) {
                    methodVisitor = methodVisitorWrapper.wrap(instrumentedType, instrumentedMethod, methodVisitor, implementationContext, typePool, writerFlags, readerFlags);
                }
                return methodVisitor;
            }

            @java.lang.Override
            @java.lang.SuppressWarnings("all")
            @javax.annotation.Generated("lombok")
            public boolean equals(final java.lang.Object o) {
                if (o == this) return true;
                if (!(o instanceof AsmVisitorWrapper.ForDeclaredMethods.Entry)) return false;
                final AsmVisitorWrapper.ForDeclaredMethods.Entry other = (AsmVisitorWrapper.ForDeclaredMethods.Entry) o;
                if (!other.canEqual((java.lang.Object) this)) return false;
                final java.lang.Object this$matcher = this.matcher;
                final java.lang.Object other$matcher = other.matcher;
                if (this$matcher == null ? other$matcher != null : !this$matcher.equals(other$matcher)) return false;
                final java.lang.Object this$methodVisitorWrappers = this.methodVisitorWrappers;
                final java.lang.Object other$methodVisitorWrappers = other.methodVisitorWrappers;
                if (this$methodVisitorWrappers == null ? other$methodVisitorWrappers != null : !this$methodVisitorWrappers.equals(other$methodVisitorWrappers)) return false;
                return true;
            }

            @java.lang.SuppressWarnings("all")
            @javax.annotation.Generated("lombok")
            protected boolean canEqual(final java.lang.Object other) {
                return other instanceof AsmVisitorWrapper.ForDeclaredMethods.Entry;
            }

            @java.lang.Override
            @java.lang.SuppressWarnings("all")
            @javax.annotation.Generated("lombok")
            public int hashCode() {
                final int PRIME = 59;
                int result = 1;
                final java.lang.Object $matcher = this.matcher;
                result = result * PRIME + ($matcher == null ? 43 : $matcher.hashCode());
                final java.lang.Object $methodVisitorWrappers = this.methodVisitorWrappers;
                result = result * PRIME + ($methodVisitorWrappers == null ? 43 : $methodVisitorWrappers.hashCode());
                return result;
            }
        }


        /**
         * A class visitor that applies the outer ASM visitor for identifying declared methods.
         */
        protected class DispatchingVisitor extends ClassVisitor {
            /**
             * The instrumented type.
             */
            private final TypeDescription instrumentedType;
            /**
             * The implementation context to use.
             */
            private final Implementation.Context implementationContext;
            /**
             * The type pool to use.
             */
            private final TypePool typePool;
            /**
             * The ASM {@link ClassWriter} reader flags to consider.
             */
            private final int writerFlags;
            /**
             * The ASM {@link ClassReader} reader flags to consider.
             */
            private final int readerFlags;
            /**
             * A mapping of fields by their name.
             */
            private final Map<String, MethodDescription> knownMethods;

            /**
             * Creates a new dispatching visitor.
             *
             * @param classVisitor          The underlying class visitor.
             * @param instrumentedType      The instrumented type.
             * @param implementationContext The implementation context to use.
             * @param typePool              The type pool to use.
             * @param methods               The methods that are declared by the instrumented type or virtually inherited.
             * @param writerFlags           The ASM {@link ClassWriter} flags to consider.
             * @param readerFlags           The ASM {@link ClassReader} flags to consider.
             */
            protected DispatchingVisitor(ClassVisitor classVisitor, TypeDescription instrumentedType, Implementation.Context implementationContext, TypePool typePool, MethodList<?> methods, int writerFlags, int readerFlags) {
                super(Opcodes.ASM5, classVisitor);
                this.instrumentedType = instrumentedType;
                this.implementationContext = implementationContext;
                this.typePool = typePool;
                this.writerFlags = writerFlags;
                this.readerFlags = readerFlags;
                knownMethods = new HashMap<String, MethodDescription>();
                for (MethodDescription methodDescription : methods) {
                    knownMethods.put(methodDescription.getInternalName() + methodDescription.getDescriptor(), methodDescription);
                }
            }

            @Override
            public MethodVisitor visitMethod(int modifiers, String internalName, String descriptor, String signature, String[] exceptions) {
                MethodVisitor methodVisitor = super.visitMethod(modifiers, internalName, descriptor, signature, exceptions);
                MethodDescription methodDescription = knownMethods.get(internalName + descriptor);
                for (Entry entry : entries) {
                    if (entry.matches(methodDescription)) {
                        methodVisitor = entry.wrap(instrumentedType, methodDescription, methodVisitor, implementationContext, typePool, writerFlags, readerFlags);
                    }
                }
                return methodVisitor;
            }
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof AsmVisitorWrapper.ForDeclaredMethods)) return false;
            final AsmVisitorWrapper.ForDeclaredMethods other = (AsmVisitorWrapper.ForDeclaredMethods) o;
            if (!other.canEqual((java.lang.Object) this)) return false;
            final java.lang.Object this$entries = this.entries;
            final java.lang.Object other$entries = other.entries;
            if (this$entries == null ? other$entries != null : !this$entries.equals(other$entries)) return false;
            if (this.writerFlags != other.writerFlags) return false;
            if (this.readerFlags != other.readerFlags) return false;
            return true;
        }

        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        protected boolean canEqual(final java.lang.Object other) {
            return other instanceof AsmVisitorWrapper.ForDeclaredMethods;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final java.lang.Object $entries = this.entries;
            result = result * PRIME + ($entries == null ? 43 : $entries.hashCode());
            result = result * PRIME + this.writerFlags;
            result = result * PRIME + this.readerFlags;
            return result;
        }
    }


    /**
     * An ordered, immutable chain of {@link AsmVisitorWrapper}s.
     */
    class Compound implements AsmVisitorWrapper {
        /**
         * The class visitor wrappers that are represented by this chain in their order. This list must not be mutated.
         */
        private final List<AsmVisitorWrapper> asmVisitorWrappers;

        /**
         * Creates a new immutable chain based on an existing list of {@link AsmVisitorWrapper}s
         * where no copy of the received array is made.
         *
         * @param asmVisitorWrapper An array of {@link AsmVisitorWrapper}s where elements
         *                          at the beginning of the list are applied first, i.e. will be at the bottom of the generated
         *                          {@link ClassVisitor}.
         */
        public Compound(AsmVisitorWrapper... asmVisitorWrapper) {
            this(Arrays.asList(asmVisitorWrapper));
        }

        /**
         * Creates a new immutable chain based on an existing list of {@link AsmVisitorWrapper}s
         * where no copy of the received list is made.
         *
         * @param asmVisitorWrappers A list of {@link AsmVisitorWrapper}s where elements
         *                           at the beginning of the list are applied first, i.e. will be at the bottom of the generated
         *                           {@link ClassVisitor}.
         */
        public Compound(List<? extends AsmVisitorWrapper> asmVisitorWrappers) {
            this.asmVisitorWrappers = new ArrayList<AsmVisitorWrapper>();
            for (AsmVisitorWrapper asmVisitorWrapper : asmVisitorWrappers) {
                if (asmVisitorWrapper instanceof Compound) {
                    this.asmVisitorWrappers.addAll(((Compound) asmVisitorWrapper).asmVisitorWrappers);
                } else if (!(asmVisitorWrapper instanceof NoOp)) {
                    this.asmVisitorWrappers.add(asmVisitorWrapper);
                }
            }
        }

        @Override
        public int mergeWriter(int flags) {
            for (AsmVisitorWrapper asmVisitorWrapper : asmVisitorWrappers) {
                flags = asmVisitorWrapper.mergeWriter(flags);
            }
            return flags;
        }

        @Override
        public int mergeReader(int flags) {
            for (AsmVisitorWrapper asmVisitorWrapper : asmVisitorWrappers) {
                flags = asmVisitorWrapper.mergeReader(flags);
            }
            return flags;
        }

        @Override
        public ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor, Implementation.Context implementationContext, TypePool typePool, FieldList<FieldDescription.InDefinedShape> fields, MethodList<?> methods, int writerFlags, int readerFlags) {
            for (AsmVisitorWrapper asmVisitorWrapper : asmVisitorWrappers) {
                classVisitor = asmVisitorWrapper.wrap(instrumentedType, classVisitor, implementationContext, typePool, fields, methods, writerFlags, readerFlags);
            }
            return classVisitor;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        public boolean equals(final java.lang.Object o) {
            if (o == this) return true;
            if (!(o instanceof AsmVisitorWrapper.Compound)) return false;
            final AsmVisitorWrapper.Compound other = (AsmVisitorWrapper.Compound) o;
            if (!other.canEqual((java.lang.Object) this)) return false;
            final java.lang.Object this$asmVisitorWrappers = this.asmVisitorWrappers;
            final java.lang.Object other$asmVisitorWrappers = other.asmVisitorWrappers;
            if (this$asmVisitorWrappers == null ? other$asmVisitorWrappers != null : !this$asmVisitorWrappers.equals(other$asmVisitorWrappers)) return false;
            return true;
        }

        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        protected boolean canEqual(final java.lang.Object other) {
            return other instanceof AsmVisitorWrapper.Compound;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("all")
        @javax.annotation.Generated("lombok")
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final java.lang.Object $asmVisitorWrappers = this.asmVisitorWrappers;
            result = result * PRIME + ($asmVisitorWrappers == null ? 43 : $asmVisitorWrappers.hashCode());
            return result;
        }
    }
}
