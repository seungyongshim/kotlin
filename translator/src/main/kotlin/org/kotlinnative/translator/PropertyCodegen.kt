package org.kotlinnative.translator

import org.jetbrains.kotlin.js.descriptorUtils.nameIfStandardType
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.resolve.BindingContext
import org.kotlinnative.translator.llvm.LLVMBuilder
import org.kotlinnative.translator.llvm.LLVMGlobalScope
import org.kotlinnative.translator.llvm.LLVMMapStandardType
import org.kotlinnative.translator.llvm.LLVMVariable


class PropertyCodegen(val state: TranslationState, val property: KtProperty, val codeBuilder: LLVMBuilder) {
    private val variableManager = state.variableManager

    fun generate() {
        val varInfo = state.bindingContext.get(BindingContext.VARIABLE, property)?.compileTimeInitializer ?: return

        val kotlinType = varInfo.type
        val value = varInfo.value
        if (kotlinType.nameIfStandardType != null) {
            val variableType = LLVMMapStandardType(property.name ?: return, kotlinType).type
            val variable = LLVMVariable(property.name.toString(), variableType, property.name.toString(), LLVMGlobalScope(), pointer = true)
            variableManager.addGlobalVariable(property.name.toString(), variable)
            codeBuilder.declareGlovalVariable(variable, variableType.parseArg(value.toString()))
        }
    }


}