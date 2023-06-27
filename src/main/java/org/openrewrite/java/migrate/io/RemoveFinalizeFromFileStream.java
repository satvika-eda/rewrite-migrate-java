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

package org.openrewrite.java.migrate.io;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

import java.util.Objects;

@Value
@EqualsAndHashCode(callSuper = true)
public class RemoveFinalizeFromFileStream extends Recipe {

    @Override
    public String getDisplayName() {
        return "Replace invocations of deprecated invocations from FileInputStream, FileOutputStream";
    }

    @Override
    public String getDescription() {
        return "Remove invocations of finalize() deprecated invocations from FileInputStream, FileOutputStream.";
    }

    @Override
    protected JavaIsoVisitor<ExecutionContext> getVisitor() {

        return new JavaIsoVisitor<ExecutionContext>() {

            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {

                J.ClassDeclaration cd = super.visitClassDeclaration(classDecl, executionContext);
                boolean extendExists = Objects.nonNull(cd.getExtends());
                if(extendExists && (cd.getExtends().toString().contains("FileInputStream") || cd.getExtends().toString().contains("FileOutputStream"))){
                    cd = cd.withBody(cd.getBody().withStatements(ListUtils.map(cd.getBody().getStatements(), mdStmt -> {
                        if (mdStmt instanceof J.MethodDeclaration) {
                            J.MethodDeclaration md = (J.MethodDeclaration) mdStmt;
                            mdStmt = md.withBody(md.getBody().withStatements(ListUtils.map(md.getBody().getStatements(), miStmt ->
                            {   if(miStmt instanceof  J.MethodInvocation){
                                J.MethodInvocation mi = (J.MethodInvocation) miStmt;
                                if (mi.getName().toString().contains("finalize")){
                                    miStmt = mi.withTemplate(JavaTemplate.builder(() -> getCursor().getParent(),  mi.getSelect().toString()+"."+"close()"+" #{any()}")
                                                    .build(),
                                            mi.getCoordinates().replace(), mi.getArguments().get(0));
                                }
                            }
                                return miStmt;
                            })));
                        }
                        return mdStmt;
                    })));
                    return cd;
                }

                return cd;
            }

        };
    }

}