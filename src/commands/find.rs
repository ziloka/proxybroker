use std::fs::File;
use clap::ArgMatches;
use std::io::prelude::*;
use futures::StreamExt;
use crate::services;

pub async fn find(sub_matches: &ArgMatches) {
  let mut file: Option<Result<File, std::io::Error>> = None;
  if sub_matches.is_present("file") {
    file = Some(File::create(sub_matches.get_one::<String>("file")
    .expect("Specify filename to write proxies to")));
  }
  let limit = sub_matches.get_one::<u64>("limit").unwrap();

  let mut counter: u64 = 0;
  let mut vecs_of_unchecked_proxies = services::collector::collect();
  'unchecked_proxies_loop: while let Some(unchecked_proxies) = vecs_of_unchecked_proxies.next().await {
    match unchecked_proxies {
      Ok(unchecked_proxies) => {
        let mut list_of_checked_proxies = services::checker::check(unchecked_proxies);
        while let Some(proxy) = list_of_checked_proxies.next().await {
          match proxy {
            Ok(proxy) => {
              if proxy.alive {
                if let Some(ref mut value) = file {
                  match &value {
                    Ok(file) => {
                      match file.clone().write(format!("{}:{}\n", proxy.host, proxy.port).as_bytes()) {
                        Ok(_is_ok) => {
                        },
                        Err(err) => println!("Could not write to file: {}", err)
                      }
                    },
                    Err(err) => println!("Could not write to file: {}", err)
                  }
                }
                counter+=1;
                println!("{}:{}", proxy.host, proxy.port);
              }
            },
            Err(_err) => {}
          }
          if counter >= *limit {
            break 'unchecked_proxies_loop;
          }
        }
      }
      Err(err) => println!("Could not join task: {}", err)
    }

  }
}