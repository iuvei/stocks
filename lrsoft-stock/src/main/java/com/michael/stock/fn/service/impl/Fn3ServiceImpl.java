package com.michael.stock.fn.service.impl;

import com.michael.base.parameter.service.ParameterContainer;
import com.michael.core.beans.BeanWrapBuilder;
import com.michael.core.beans.BeanWrapCallback;
import com.michael.core.hibernate.HibernateUtils;
import com.michael.core.hibernate.validator.ValidatorUtils;
import com.michael.core.pager.PageVo;
import com.michael.stock.db.domain.DB;
import com.michael.stock.fn.bo.Fn3Bo;
import com.michael.stock.fn.dao.Fn3Dao;
import com.michael.stock.fn.domain.Fn3;
import com.michael.stock.fn.service.Fn3Service;
import com.michael.stock.fn.vo.Fn3Vo;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author Michael
 */
@Service("fn3Service")
public class Fn3ServiceImpl implements Fn3Service, BeanWrapCallback<Fn3, Fn3Vo> {
    @Resource
    private Fn3Dao fn3Dao;

    @Override
    public String save(Fn3 fn3) {
        validate(fn3);
        String id = fn3Dao.save(fn3);
        return id;
    }

    @Override
    public void update(Fn3 fn3) {
        validate(fn3);
        fn3Dao.update(fn3);
    }

    private void validate(Fn3 fn3) {
        ValidatorUtils.validate(fn3);
    }

    @Override
    public PageVo pageQuery(Fn3Bo bo) {
        PageVo vo = new PageVo();
        Long total = fn3Dao.getTotal(bo);
        vo.setTotal(total);
        if (total == null || total == 0) return vo;
        List<Fn3> fn3List = fn3Dao.pageQuery(bo);
        List<Fn3Vo> vos = BeanWrapBuilder.newInstance()
                .setCallback(this)
                .wrapList(fn3List, Fn3Vo.class);
        vo.setData(vos);
        return vo;
    }


    @Override
    public Fn3Vo findById(String id) {
        Fn3 fn3 = fn3Dao.findById(id);
        return BeanWrapBuilder.newInstance()
                .wrap(fn3, Fn3Vo.class);
    }

    @Override
    public void deleteByIds(String[] ids) {
        if (ids == null || ids.length == 0) return;
        for (String id : ids) {
            fn3Dao.deleteById(id);
        }
    }

    @Override
    public List<Fn3Vo> query(Fn3Bo bo) {
        List<Fn3> fn3List = fn3Dao.query(bo);
        List<Fn3Vo> vos = BeanWrapBuilder.newInstance()
                .setCallback(this)
                .wrapList(fn3List, Fn3Vo.class);
        return vos;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void reset() {
        int fn = 10;    // 系数范围
        long f = 1000L * 60 * 60 * 24;
        final Session session = HibernateUtils.getSession(false);
        Logger logger = Logger.getLogger(Fn4ServiceImpl.class);
        logger.info(" *****************   RESET Fn3 : Start ************************** ");
        // 加载所有的日期
        // 删除原有数据
        session.createQuery("delete from " + Fn3.class.getName()).executeUpdate();
        // 加载所有的日期
        for (int i = 1; i < 5; i++) {
            logger.info(" *****************   RESET Fn3 : " + i + " ************************** ");
            List<Date> dates = session
                    .createQuery("select d.dbDate from " + DB.class.getName() + " d where d.type=? order by d.dbDate asc")
                    .setParameter(0, i + "")
                    .list();
            if (dates.isEmpty()) {
                continue;
            }
            int size = dates.size();
            int f1 = 0, f2 = 1, f3 = 2;
            for (; f1 < size - 3; f1++) {   // 第一层游标
                long d1 = dates.get(f1).getTime();
                for (f2 = f1 + 1; f2 < size - 2; f2++) {
                    long d2 = dates.get(f2).getTime();
                    for (f3 = f2 + 1; f3 < size - 1; f3++) {
                        long d3 = dates.get(f3).getTime();
                        for (int x = -fn; x <= fn; x++) {
                            Date bk = new Date(d1 + d2 - d3 + f * x);
                            Fn3 fn3 = new Fn3();
                            fn3.setA1(new Date(d1));
                            fn3.setA2(new Date(d2));
                            fn3.setA3(new Date(d3));
                            fn3.setBk(bk);
                            fn3.setType(i);
                            fn3.setFn(x);
                            session.save(fn3);
                        }
                        session.flush();
                        session.clear();
                    }
                }
            }
        }
        logger.info(" *****************   RESET Fn3 : End ************************** ");
    }

    @Override
    public void doCallback(Fn3 fn3, Fn3Vo vo) {
        ParameterContainer container = ParameterContainer.getInstance();

    }
}