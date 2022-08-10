use std::net::IpAddr;
#[cfg(feature = "maxminddb")]
use maxminddb::Reader;

// https://github.com/oschwald/maxminddb-rust/blob/main/examples/lookup.rs
#[cfg(feature = "maxminddb")]
// pub fn lookup(ip: String) {
pub fn lookup<'a>(reader: Reader<std::borrow::Cow<'a, [u8]>>, ip: String) -> Result<maxminddb::geoip2::Country<'a>, maxminddb::MaxMindDBError> {
  reader.lookup::<maxminddb::geoip2::Country>(ip.parse::<IpAddr>().unwrap())
}