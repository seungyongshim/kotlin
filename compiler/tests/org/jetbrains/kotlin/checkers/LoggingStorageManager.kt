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

package org.jetbrains.kotlin.checkers

import org.jetbrains.kotlin.storage.ObservableStorageManager
import org.jetbrains.kotlin.storage.StorageManager
import java.lang.reflect.Field
import java.lang.reflect.GenericDeclaration

public class LoggingStorageManager(
        private val delegate: StorageManager,
        private val callHandler: (lambda: Any, call: LoggingStorageManager.CallData?) -> Unit) : ObservableStorageManager(delegate) {

    public class CallData(
            val fieldOwner: Any?,
            val field: Field?,
            val lambdaCreatedIn: GenericDeclaration?,
            val arguments: List<Any?>,
            val result: Any?
    )

    // Creating objects here because we need a reference to it
    override val <T> (() -> T).observable: () -> T
        get() = object : () -> T {
            override fun invoke(): T {
                val result = this@observable()
                callHandler(this@observable, computeCallerData(this@observable, this, listOf(), result))
                return result
            }
        }

    // Creating objects here because we need a reference to it
    override val <K, V> ((K) -> V).observable: (K) -> V
        get() = object : (K) -> V {
            override fun invoke(p1: K): V {
                val result = this@observable(p1)
                callHandler(this@observable, computeCallerData(this@observable, this, listOf(p1), result))
                return result
            }
        }

    private fun computeCallerData(lambda: Any, wrapper: Any, arguments: List<Any?>, result: Any?): CallData {
        val lambdaClass = lambda.javaClass

        val outerClass: Class<out Any?>? = lambdaClass.getEnclosingClass()

        // fields named "this" or "this$0"
        val referenceToOuter = lambdaClass.getAllDeclaredFields().firstOrNull {
            field ->
            field.getType() == outerClass && field.getName()!!.contains("this")
        }
        referenceToOuter?.setAccessible(true)

        val outerInstance = referenceToOuter?.get(lambda)

        fun Class<*>.findFunctionField(): Field? {
            return this.getAllDeclaredFields().firstOrNull {
                it.getType()?.getName()?.startsWith("kotlin.Function") ?: false
            }
        }
        val containingField = if (outerInstance == null) null
                              else outerClass?.getAllDeclaredFields()?.firstOrNull {
                                  field ->
                                  field.setAccessible(true)
                                  val value = field.get(outerInstance)
                                  if (value == null) return@firstOrNull false

                                  val valueClass = value.javaClass
                                  val functionField = valueClass.findFunctionField()
                                  if (functionField == null) return@firstOrNull false

                                  functionField.setAccessible(true)
                                  val functionValue = functionField.get(value)
                                  functionValue == wrapper
                              }

        if (containingField == null) {
            val wrappedLambdaField = lambdaClass.findFunctionField()
            if (wrappedLambdaField != null) {
                wrappedLambdaField.setAccessible(true)
                val wrappedLambda = wrappedLambdaField.get(lambda)
                return CallData(outerInstance, null, enclosingEntity(wrappedLambda.javaClass), arguments, result)
            }
        }

        val enclosingEntity = enclosingEntity(lambdaClass)

        return CallData(outerInstance, containingField, enclosingEntity, arguments, result)
    }

    private fun enclosingEntity(_class: Class<Any>): GenericDeclaration? {
        val result = _class.getEnclosingConstructor()
            ?: _class.getEnclosingMethod()
            ?: _class.getEnclosingClass()

        return result as GenericDeclaration?
    }

    private fun Class<*>.getAllDeclaredFields(): List<Field> {
        val result = arrayListOf<Field>()

        var c = this
        while (true) {
            result.addAll(c.getDeclaredFields().toList())
            @Suppress("UNCHECKED_CAST")
            val superClass = (c as Class<Any>).getSuperclass() as Class<Any>?
            if (superClass == null) break
            if (c == superClass) break
            c = superClass
        }

        return result
    }
}
