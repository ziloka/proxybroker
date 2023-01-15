use crate::services;
use clap::ArgMatches;
use std::fs::File;
use std::io::prelude::*;
use tokio::sync::mpsc::channel;

pub fn find(sub_matches: &ArgMatches) -> Result<(), Box<dyn std::error::Error>> {
    let mut file: Option<Result<File, std::io::Error>> = None;
    let file_str = sub_matches.get_one::<String>("file");
    if let Some(file_path) = file_str {
        file = Some(File::create(file_path));
    }

    let limit = sub_matches.get_one::<u64>("limit").unwrap();
    println!("limit: {}", limit);
    let (unchecked_proxies_tx, mut unchecked_proxies_rx) =
        channel::<Vec<crate::services::collector::Proxy>>(100);
    let (checked_proxies_tx, mut checked_proxies_rx) =
        channel::<crate::services::checker::CheckProxyResponse>(100);
    services::collector::collect(unchecked_proxies_tx);
    let mut counter: u64 = 0;

    loop {
        if let Ok(proxies) = unchecked_proxies_rx.try_recv() {
            services::checker::check(checked_proxies_tx.clone(), proxies)
        }
        if let Ok(proxy) = checked_proxies_rx.try_recv() {
            if proxy.alive {
                counter += 1;
                println!("{}:{}", proxy.host, proxy.port);
                if let Some(ref file) = file {
                    match file {
                        Ok(file) => {
                            if let Err(err) = file
                                .clone()
                                .write(format!("{}:{}\n", proxy.host, proxy.port).as_bytes())
                            {
                                println!("Could not write to file: {}", err);
                            };
                        }
                        Err(err) => println!("Could not grab file from result object: {}", err),
                    }
                }
                if counter >= *limit {
                    break;
                }
            }
        }
    }
    Ok(())
}
