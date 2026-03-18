<?php

namespace App\Controller;

use Doctrine\DBAL\Exception;
use Doctrine\ORM\EntityManagerInterface;
use League\Flysystem\FilesystemException;
use League\Flysystem\FilesystemOperator;
use Psr\Cache\CacheItemPoolInterface;
use Psr\Cache\InvalidArgumentException;
use Psr\Log\LoggerInterface;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;

class HomeController extends AbstractController
{
    #[Route('/', name: 'home')]
    public function home(EntityManagerInterface $orm, CacheItemPoolInterface $app, FilesystemOperator $persistentStorage, LoggerInterface $logger): Response
    {

        try {
            $status['database'] = is_string($orm->getConnection()->getDatabase());
        } catch (Exception $e) {
            $logger->warning('Error while trying to use database: "{message}"', [
                'message' => $e->getMessage(),
                'exception' => $e,
            ]);
            $status['database'] = false;
        }

        try {
            $item = $app->getItem('foo');
            $item->set('check');
            $app->save($item);
            // if the cache is not working, there is no exception to not interrupt the program flow, but nothing is stored
            $status['kvs'] = $app->hasItem('foo');
        } catch (InvalidArgumentException $e) {
            $logger->warning('Error while trying to use key value store: "{message}"', [
                'message' => $e->getMessage(),
                'exception' => $e,
            ]);
            $status['kvs'] = false;
        }

        try {
            $persistentStorage->write('/test-file', 'Symfony Test');
            $persistentStorage->has('/test-file');
            $status['storage'] = true;
        } catch (FilesystemException $e) {
            $logger->warning('Error while trying to connect to filesystem: "{message}"', [
                'message' => $e->getMessage(),
                'exception' => $e,
            ]);
            $status['storage'] = false;
        }

        return $this->render('deploio.html.twig', [
            'status' => $status,
        ]);
    }
}
