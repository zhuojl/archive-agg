
package com.zhuojl.map.reduce;

import com.google.common.collect.Range;

import com.zhuojl.map.reduce.annotation.SpelArchiveKeyExpression;
import com.zhuojl.map.reduce.archivekey.ArchiveKeyResolver;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * @author TaoYu
 * @since 2.5.0
 */
@Service(SpElExpressionKeyResolver.SPEL_EXPRESSION_RESOLVER)
public class SpElExpressionKeyResolver implements ArchiveKeyResolver<SystemArchiveKey> {
  public static final String SPEL_EXPRESSION_RESOLVER = "SpElExpressionResolver";

  @Override
  public Object[] rebuild(SystemArchiveKey archiveKey, Object... params) {
    throw new UnsupportedOperationException("spEl can not rebuild ");
  }

  /**
   * 参数发现器
   */
  private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
  /**
   * Express语法解析器
   */
  private static final ExpressionParser PARSER = new SpelExpressionParser();


  @Override
  public SystemArchiveKey extract(Method method, Object[] args) {

    SpelArchiveKeyExpression archiveKey = method.getAnnotation(SpelArchiveKeyExpression.class);


    EvaluationContext context = new MethodBasedEvaluationContext(null, method, args,
        NAME_DISCOVERER);
    int high = Integer.parseInt(PARSER.parseExpression(archiveKey.high()).getValue(context).toString());
    int low = Integer.parseInt(PARSER.parseExpression(archiveKey.low()).getValue(context).toString());

    return new SystemArchiveKey(Range.closed(low, high));
  }
}
