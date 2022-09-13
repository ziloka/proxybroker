use crate::services;
use clap::ArgMatches;
use crossbeam::channel::{bounded, unbounded};
use std::fs::File;
use std::time::SystemTime;
use std::io::prelude::*;

pub fn find(sub_matches: &ArgMatches) {

  // puffin::set_scopes_on(true);

    let start = SystemTime::now();

    let mut file: Option<Result<File, std::io::Error>> = None;
    if sub_matches.is_present("file") {
        file = Some(File::create(
            sub_matches
                .get_one::<String>("file")
                .expect("Specify filename to write proxies to"),
        ));
    }

    let limit = sub_matches.get_one::<u64>("limit").unwrap();
    let (unchecked_proxies_tx, unchecked_proxies_rx) =
    unbounded::<Vec<crate::services::collector::Proxy>>();
    let (checked_proxies_tx, checked_proxies_rx) =
    unbounded::<crate::services::checker::CheckProxyResponse>();
    services::collector::collect(unchecked_proxies_tx.clone()); // if not cloned throws Disconnected Error, otherwise throws Empty
    println!("finished collect method at {}ms", start.elapsed().expect("Duration").as_millis());
    let mut counter: u64 = 0;

    loop {
        match unchecked_proxies_rx.try_recv() {
            Ok(proxies) => {
              println!("sending proxies to check service after {}s", start.elapsed().expect("Duration").as_secs());
              services::checker::check(checked_proxies_tx.clone(), proxies);
            },
            Err(_) => {} // Err(e) => println!("Unchecked Proxies queue Error: {e}")
        }
        match checked_proxies_rx.try_recv() {
            Ok(proxy) => {
                if proxy.alive {
                    counter += 1;
                    println!("receieved {}:{} after {}s", proxy.host, proxy.port, start.elapsed().expect("Duration").as_secs());
                    if counter >= *limit {
                        std::process::exit(0);
                    }
                }
            }
            Err(_) => {} // Err(e) => println!("Checked Proxies queue Error: {e}")
        }
    }
}
