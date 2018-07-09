package nl.renedegroot.intellij.plugin.skullsbones

import com.intellij.codeHighlighting.Pass
import com.intellij.codeInsight.daemon.GutterIconDescriptor
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.util.Function
import org.jetbrains.kotlin.idea.caches.resolve.analyzeFully
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.resolve.calls.callUtil.getType
import org.jetbrains.kotlin.types.FlexibleType
import org.jetbrains.kotlin.types.typeUtil.makeNotNullable
import javax.swing.Icon


class SkullBonesMarkerProvider : GutterIconDescriptor(), LineMarkerProvider {

    override fun getName() = "SkullBones"

    override fun getIcon(): Icon = Icons.skullBones

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element is KtNameReferenceExpression) {
            val type = element.getType(element.analyzeFully())
            if (type is FlexibleType) {
                val lowerBound = type.lowerBound
                val upperBound = type.upperBound
                if (upperBound.isMarkedNullable
                        && upperBound.makeNotNullable() == lowerBound) {
                    return createLineMarker(element)
                }
            }
        }
        return null
    }

    private fun createLineMarker(referenceExpression: KtNameReferenceExpression): LineMarkerInfo<*>? = LineMarkerInfo(
            referenceExpression,
            referenceExpression.textRange,
            icon,
            Pass.LINE_MARKERS,
            createToolTip(),
            null,
            GutterIconRenderer.Alignment.LEFT)

    private fun createToolTip(): Function<in KtNameReferenceExpression, String> =
            Function { element: KtNameReferenceExpression ->
                val type = element.getType(element.analyzeFully()) as FlexibleType
                "Missing nullability on '${element.text}', could be '${type.lowerBound}' or '${type.upperBound}'."
            }

    override fun collectSlowLineMarkers(elements: MutableList<PsiElement>, results: MutableCollection<LineMarkerInfo<PsiElement>>) {
    }

}