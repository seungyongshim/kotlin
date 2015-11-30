/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.refactoring.introduce.introduceVariable

import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.idea.analysis.analyzeInContext
import org.jetbrains.kotlin.idea.caches.resolve.getResolutionFacade
import org.jetbrains.kotlin.idea.util.getResolutionScope
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.createExpressionByPattern
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.util.isValidOperator

fun getApplicableComponentFunctions(expression: KtExpression): List<FunctionDescriptor> {
    val facade = expression.getResolutionFacade()
    val scope = expression.getResolutionScope(facade.analyze(expression), facade)

    val psiFactory = KtPsiFactory(expression)
    @Suppress("UNCHECKED_CAST")
    return sequence(1) { it + 1 }
            .map {
                val componentCallExpr = psiFactory.createExpressionByPattern("$0.$1", expression, "component$it()")
                val newContext = componentCallExpr.analyzeInContext(scope, expression)
                componentCallExpr.getResolvedCall(newContext)?.resultingDescriptor as? FunctionDescriptor
            }
            .takeWhile { it != null && it.isValidOperator() }
            .toList() as List<FunctionDescriptor>
}