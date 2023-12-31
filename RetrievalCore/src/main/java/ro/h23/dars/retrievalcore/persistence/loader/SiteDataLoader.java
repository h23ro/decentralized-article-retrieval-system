package ro.h23.dars.retrievalcore.persistence.loader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.h23.dars.retrievalcore.api.publicapi.controller.AllPublicController;
import ro.h23.dars.retrievalcore.config.exception.ServerListReaderException;
import ro.h23.dars.retrievalcore.config.exception.SiteInfoReaderException;
import ro.h23.dars.retrievalcore.config.model.SiteInfo;
import ro.h23.dars.retrievalcore.config.service.ServerListReaderService;
import ro.h23.dars.retrievalcore.config.service.SiteInfoReaderService;
import ro.h23.dars.retrievalcore.persistence.repository.SiteRepository;
import ro.h23.dars.retrievalcore.persistence.service.SiteDataConverterService;
import ro.h23.dars.retrievalcore.persistence.model.Site;

import java.util.List;

@Component
public class SiteDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LogManager.getLogger(SiteDataLoader.class);

    private final SiteRepository siteRepository;

    private final SiteInfoReaderService siteInfoReaderService;

    private final SiteDataConverterService siteDataConverterService;

    private final ServerListReaderService serverListReaderService;

    private final AllPublicController allPublicController;

    public SiteDataLoader(SiteRepository siteRepository, SiteInfoReaderService siteInfoReaderService, SiteDataConverterService siteDataConverterService, ServerListReaderService serverListReaderService, AllPublicController allPublicController) {
        this.siteRepository = siteRepository;
        this.siteInfoReaderService = siteInfoReaderService;
        this.siteDataConverterService = siteDataConverterService;
        this.serverListReaderService = serverListReaderService;
        this.allPublicController = allPublicController;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {

        logger.info("Loading server list.");

        try {
            List<String> serverList = serverListReaderService.read();
            logger.info("Loaded serverList: " + serverList);
            allPublicController.setServerList(serverList);
        } catch (ServerListReaderException e) {
            logger.warn(e.getMessage());
        }

        logger.info("Loading site data.");

        try {
            List<SiteInfo> siteInfoList = siteInfoReaderService.read();
            //System.out.println(siteInfoList);
            //System.exit(1);
            for (SiteInfo siteInfo : siteInfoList) {
                Site site = siteRepository.findOneByName(siteInfo.getName());
                site = siteDataConverterService.siteInfoToExistingSite(siteInfo, site);
                siteRepository.save(site);
            }
        } catch (SiteInfoReaderException e) {
            throw new RuntimeException(e);
        }
    }

}