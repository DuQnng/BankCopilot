package com.idea.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class OperateLogAspect {
//    @Autowired
//    private OperateLogMapper operateLogMapper;
//    // 定义切点：拦截controller包下所有方法
//    private EmpLogMapper empLogMapper;
//    @Pointcut("@annotation(com.idea.anno.Log)||@annotation(org.springframework.web.bind.annotation.PutMapping)" +
//            "||@annotation(org.springframework.web.bind.annotation.PostMapping)" +
//            "||@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
//    public void operateLogPointcut() {}
//
//    @Around("operateLogPointcut()")
//    public Object recordOperateLog(ProceedingJoinPoint joinPoint) throws Throwable {
//        long startTime = System.currentTimeMillis();
//
//        Object result = joinPoint.proceed();
//        long endTime = System.currentTimeMillis();
//        long costTime = endTime - startTime;
//
//        OperateLog olog = new OperateLog();
//        olog.setOperateEmpId(getCurrentUserId());
//        olog.setOperateTime(LocalDateTime.now());
//        olog.setClassName(joinPoint.getSignature().getClass().getName());
//        olog.setMethodName(joinPoint.getSignature().getName());
//        olog.setMethodParams(Arrays.toString(joinPoint.getArgs()));
//        olog.setReturnValue(result!=null?result.toString():"void");
//        olog.setCostTime(costTime);
//
//
//        operateLogMapper.insert_log(olog);
//
//
//        return result;
//    }
//
//    private Integer getCurrentUserId() {
//        return CurrentHolder.getCurrentId();
//    }


}