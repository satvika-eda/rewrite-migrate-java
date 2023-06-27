/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openrewrite.java.migrate.lang;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.search.UsesType;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;

import java.util.Objects;

public class RemoveDeprecatedRuntime extends Recipe {

    public static final String TRACE_INSTRUCTIONS = "traceInstructions";
    private static final String JAVA_LANG_RUNTIME = "java.lang.Runtime";
    public static final String TRACE_METHOD_CALLS = "traceMethodCalls";

    @Override
    public String getDisplayName() {
        return "Remove deprecated statements from Runtime module";
    }

    @Override
    public String getDescription() {
        return "Remove deprecated invocations of Runtime.traceInstructions() and Runtime.traceMethodCalls() which have no alternatives needed.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {


        return Preconditions.check(new UsesType<>(JAVA_LANG_RUNTIME, false),
                new JavaIsoVisitor<ExecutionContext>() {
                    @Override
                    public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
                        J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);
                        return (Objects.nonNull(mi.getSelect()) && TypeUtils.isAssignableTo(JAVA_LANG_RUNTIME, mi.getSelect().getType())
                                && (TRACE_INSTRUCTIONS.equals(mi.getSimpleName()) || TRACE_METHOD_CALLS.equals(mi.getSimpleName()))
                                ? null : mi);

                    }
                });
    }

}